package com.icicle.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.icicle.goods.feign.SkuFeign;
import com.icicle.goods.pojo.Sku;
import com.icicle.search.dao.SkuESMapper;
import com.icicle.search.pojo.SkuInfo;
import com.icicle.search.service.SkuService;
import entity.Result;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @Author Max
 * @Date 19:48 2019/8/31
 * @Description：实现Sku数据导入到ES中
 **/
@Service
public class SkuServiceImpl implements SkuService {

    @Autowired(required = false)
    private SkuFeign skuFeign;

    @Autowired
    private SkuESMapper skuESMapper;

    /****
     * ElasticsearchTemplate  ：  可以实现索引库的增删改查[高级搜索]
     */
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 多条件搜索  要学会猜
     * @param searchMap 前端传入的搜索条件 包括关键字 规格
     * @return
     */
    @Override
    public Map<String,Object> search(Map<String, String> searchMap) {

        /**
         * 搜索条件的封装
         */
        NativeSearchQueryBuilder nativeSearchQueryBuilder = buildBasicQuery(searchMap);

        //集合搜索
        Map<String, Object> resultMap = searchList(nativeSearchQueryBuilder);

        //分组搜索
        Map<String, Object> groupMap = searchGroupList(nativeSearchQueryBuilder, searchMap);
        resultMap.putAll(groupMap);  //讲一个集合直接放进另一个集合

        return resultMap;
    }

    /**
     * 搜索条件的封装
     * @param searchMap 搜索条件
     * @return
     */
    private NativeSearchQueryBuilder buildBasicQuery(Map<String, String> searchMap) {
        //首先封装搜索条件  搜索条件构建对象，用于封装各种搜索条件
        //NativeSearchQuery 需要构建出这个
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        //那么这里就需要用到组合查询了 BoolQuery  must  must_not should
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //开始封装条件  首先判断传入的条件是否为空
        if (searchMap != null && searchMap.size() > 0) {
            //根据关键词搜索
            String keywords = searchMap.get("keywords");
            //如果关键词不为空，则搜索关键词数据
            if (!StringUtils.isEmpty(keywords)) {
//                nativeSearchQueryBuilder.withQuery(QueryBuilders.queryStringQuery(keywords).field("name"));
                boolQueryBuilder.must(QueryBuilders.queryStringQuery(keywords).field("name"));
            }
            //输入了分类 根据分类名字搜索  分类是不分词 直接词条搜索
            if (!StringUtils.isEmpty(searchMap.get("category"))){
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName",searchMap.get("category")));
            }
            //输入了品牌  根据品牌搜索  品牌也不分词 直接词条搜索
            if (!StringUtils.isEmpty(searchMap.get("brand"))){
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName",searchMap.get("brand")));
            }

            //规格过滤设置 :spec_网络=联通3G&spec_颜色=红
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                String key = entry.getKey();
                //如果key以spec_开始，则表示规格筛选查询
                if (key.startsWith("spec_")){
                    //规格条件的值  这里特殊的关键字处理 将转义字符替换成空
//                    String value = entry.getValue();
                    String value = entry.getValue().replace("\\","");
                    //spec_网络  spec_前5个要去掉
                    boolQueryBuilder.must(QueryBuilders.termQuery("specMap."+key.substring(5)+".keyword",value));
                }
            }

            //价格区间过滤  rangeQuery范围查询
            //price  0-500元 500-1000元 1000-1500元 1500-2000元 2000-3000元 3000元以上
            String price = searchMap.get("price");
            if (!StringUtils.isEmpty(price)) {
                //去掉中文元和以上  0-500 500-1000 1000-1500 1500-2000 2000-3000 3000
                price = price.replace("元", "").replace("以上", "");
                //price[] 根据-进行分割 [0,500][500,1000]............................[3000] [x，y]类型
                String[] prices = price.split("-");
                //这里是有逻辑存在的 x一定不为空 y有可能为空
                //有可能没有价格区间
                if (prices.length>0) { //这个代表肯定有一个数
                    //price > prices[0]
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(Integer.parseInt(prices[0])));
                    if (prices.length==2){
                        // price<=prices[1]
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(Integer.parseInt(prices[1])));
                    }
                }
            }
            //排序实现  [这里的实现是按照价格区间来的]
            String sortField = searchMap.get("sortField"); //指定排序的域
            String sortRule = searchMap.get("sortRule");  //指定排序的规则 ASC DESC
            if (!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortRule)) {
                nativeSearchQueryBuilder.withSort(
                        new FieldSortBuilder(sortField).   //指定排序的域
                                order(SortOrder.valueOf(sortRule)));
            }
        }

        //分页，用户如果不传入分页参数，则默认第一页  测试阶段 默认查询3条数据
        Integer pageNum = converterPage(searchMap);  //默认第一页
        Integer size = 10;  //默认查询的数据条数
        nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNum-1,size)); //这里默认从页码0开始  前端实际传过来的需要减1

        //将boolQueryBuilder填充给nativeSearchQueryBuilder
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        return nativeSearchQueryBuilder;
    }

    /**
     * 接收前端传入的分页参数  没有传入分页参数就抛个异常 返回1  抛异常的操作要会
     * @param searchMap
     * @return
     */
    private Integer converterPage(Map<String,String> searchMap){
        if (searchMap!=null){
            String pageNum = searchMap.get("pageNum");
                if(!StringUtils.isEmpty(pageNum)){
                    return Integer.parseInt(pageNum);
            }
        }
        return 1;
    }

    /**
     * 分组查询-->分类分组、品牌分组、规格分组  实现封装
     * @param nativeSearchQueryBuilder 条件构建对象
     * @return
     */
    private Map<String, Object> searchGroupList(NativeSearchQueryBuilder nativeSearchQueryBuilder, Map<String,String> searchMap) {
        /**
         * 分组查询分类集合  根据关键子查询 分类名称  然后根据分类名称分组即可查询出来了
         * .addAggregation():添加一个聚合操作  高级查询 groupby
         * 参数一：.terms(参数一) 叫做取别名
         * 参数二：field(参数二) 表示根据哪个域进行分组查询
         */
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        }

        //分组查询品牌集合
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        }

        //分组查询规格集合
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(10000));

        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

        /***
         * 获取分组数据
         * aggregatedPage.getAggregations():获取的是集合,可以根据多个域进行分组
         * .get("skuCategory"):获取指定域的集合数  [手机,家用电器,手机配件]
         *
         * 本来返回的是一个 返回一个集合 是其中一个实现类而已
         */

        //定义一个Map 封装所有搜索后的分组信息
        Map<String,Object> groupResultMap = new HashMap<>();

        //当用户选择了分类，将分类作为搜索条件，则不需要对分类进行分组搜索，因为分组搜索的数据是用于显示分类搜索条件的
        //分类->searchMap->category
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            StringTerms categoryTerms = aggregatedPage.getAggregations().get("skuCategory");
            //获取分类分组集合数据
            List<String> categoryList = getGroupList(categoryTerms);
            groupResultMap.put("categoryList",categoryList);
        }

        //当用户选择了品牌，将品牌作为搜索条件，则不需要对品牌进行分组搜索，因为分组搜索的数据是用来显示品牌搜索条件的
        //品牌->searchMap->category
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            StringTerms brandTerms = aggregatedPage.getAggregations().get("skuBrand");
            //获取品牌分组集合数据
            List<String> brandList = getGroupList(brandTerms);
            groupResultMap.put("brandList",brandList);
        }
        //获取规格分组集合数据->实现合并操作
        StringTerms specTerms = aggregatedPage.getAggregations().get("skuSpec");
        List<String> specList = getGroupList(specTerms);
        //实现合并操作
        Map<String, Set<String>> specMap = putAllSpec(specList);
        groupResultMap.put("specList",specMap);

        return groupResultMap;
    }

    /**
     * 获取分组集合数据  抽取 公共复用方法
     * @param stringTerms
     * @return
     */
    private List<String> getGroupList(StringTerms stringTerms) {
        List<String> groupList = new ArrayList<>();  //查询出来的分类集合
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String fieldName = bucket.getKeyAsString(); //其中一个分类的名字  放入集合中返回前端
            groupList.add(fieldName);
        }
        return groupList;
    }

    /**
     * 规格汇总合并 封装
     * @param specList
     * @return
     */
    private Map<String, Set<String>> putAllSpec(List<String> specList) {
        //规格汇总合并
        //合并后的map对象：将每一个map对象合并成一个Map<String , Set<String>>
        Map<String, Set<String>> allSpec = new HashMap<>();
        //1.循环specList spec={"手机屏幕尺寸":"5.5寸","网络":"联通2G","颜色":"紫","测试":"实施","机身内存":"128G","存储":"16G","像素":"800万像素"}
        for (String spec : specList) {
            //2.将每一个json字符串转化成Map  为何要转成Map呢 因为好取值
            Map<String,String> specMap = JSON.parseObject(spec,Map.class);
            //3.合并流程 循环所有的Map
            for (Map.Entry<String, String> entry : specMap.entrySet()) {
                //4.1 取出当前的Map，并且获取对应的key 以及对应的 value
                String key = entry.getKey();  //规格的名字
                String value = entry.getValue();  //规格的值

                //4.2将当前循环的数据合并到一个Map<string ,set<string>>中  value选择set主要是为了去重
                //从allSpec中获取当前规格对应的Set集合数据
                Set<String> specSet = allSpec.get(key);
                //这里为啥要获取下呢 因为key值相同的数据会覆盖 这样获取 判断下没有就new hashset  有的话就直接添加进去 就不怕覆盖了 【重点】
                if (specSet == null) {
                    //表示 合并的Map中没有那个这个key所对应的value  那么就new一个
                    specSet = new HashSet<>();
                }
                specSet.add(value);
                //执行合并
                allSpec.put(key,specSet);
            }
        }
        return allSpec;
    }

    /**
     * 结果集搜索封装
     * @param nativeSearchQueryBuilder 条件构建对象
     * @return
     */
    private Map<String, Object> searchList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        //高亮配置  记住一定是HighlightBuilder.Field
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");  //指定高亮域
        //前缀  <em style="color:red;">
        field.preTags("<em style=\"color:red;\">");
        //后缀
        field.postTags("</em>");
        //碎片长度  关键词数据长度  显示数据
        field.fragmentSize(100);
        //添加高亮
        nativeSearchQueryBuilder.withHighlightFields(field);

        /*****
         * 执行搜索,响应结果给我
         * 1)搜索条件封装对象
         * 2)搜索的结果集(集合数据)需要转换的类型  [中间展示的商品的数据类型]
         * 3)AggregatedPage<SkuInfo>:  就是这个page  搜索结果集的封装  里面有很多数据
         */
//        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(
                nativeSearchQueryBuilder.build(),  //搜索条件封装
                SkuInfo.class,   //数据集合要转换的类型
                new SearchResultMapper() {  //执行搜索后，将数据结果集封装到该对象中
                    @Override
                    public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                        //存储所有的转换后的高亮数据对象
                        List<T> resList = new ArrayList<>();

                        //执行查询  获取所有数据->结果集[非高亮数据|高亮数据]
                        for (SearchHit hit : response.getHits()) {
                            //分析结果集数据 得到非高亮数据
                            SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);

                            //分析结果集数据，获取高亮数据->只有某个域的高亮数据
                            HighlightField highlightField = hit.getHighlightFields().get("name");

                            if (highlightField!=null && highlightField.getFragments()!=null){
                                //高亮数据读出
                                Text[] fragments = highlightField.getFragments();
                                //得到的是一个数组 循环 转换成string  --string[]
                                StringBuffer buffer = new StringBuffer();
                                for (Text fragment : fragments) {
                                    buffer.append(fragment.toString());
                                }
                                //高亮数据替换非高亮数据
                                skuInfo.setName(buffer.toString());
                            }
                            //将高亮数据添加到集合中 方便返回
                            resList.add((T) skuInfo);
                        }
                        //将数据返回
                        /***
                         * 1)搜索的集合数据：(携带高亮)List<T> content
                         * 2)分页对象信息：Pageable pageable
                         * 3)搜索记录的总条数：long total
                         */
                        return new AggregatedPageImpl<T>(resList,pageable,response.getHits().getTotalHits());
                    }
                }
        );

        //分页参数总记录数
        long totalElements = page.getTotalElements();

        //总页数
        int totalPages = page.getTotalPages();

        //获取数据结果集
        List<SkuInfo> skuInfoList = page.getContent();


        //封装一个Map返回
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("rows",skuInfoList);  //页面需要的数据
        resultMap.put("total",totalElements); //页面总记录数
        resultMap.put("totalPage",totalPages); //总页数

        //获取搜索封装信息 [这里就是前边默认搜索10页 如果未传当前页就从默认第一页开始  用于显示分页]
        NativeSearchQuery searchQuery = nativeSearchQueryBuilder.build();
        Pageable pageable = searchQuery.getPageable();
        int pageNum= pageable.getPageNumber();  //当前页
        int pageSize = pageable.getPageSize();  //每页显示数据条数
        resultMap.put("pageNum",pageNum);  //注意这里默认是从0开始 controller层需要将其加1显示
        resultMap.put("pageSize",pageSize);

        return resultMap;
    }



    /**
     * 导入sku数据到es里边
     */
    @Override
    public void importSku() {
        //使用feign远程调用 查询sku列表 审核通过的
        Result<List<Sku>> skuResult = skuFeign.findByStatus("1");
        /****
         * 将List<Sku>转成List<SkuInfo>
         * List<Sku>->[{skuJSON}]->List<SkuInfo>   字符串可以转化成任何类型 合情合理合法
         * 将查询出来的数据 转成 search需要的SkuInfo 因为这个类上集成es加上了document注解
         */
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(skuResult.getData()), SkuInfo.class);

        //搜索的时候也需要根据spec 规格去搜索 但是规格在数据库中的格式 {"电视音响效果":"立体声","电视屏幕尺寸":"20英寸","尺码":"165"}
        //获取spec -> Map(String)->Map类型
        for (SkuInfo skuInfo : skuInfoList) {
            Map<String,Object> specMap = JSON.parseObject(skuInfo.getSpec(), Map.class);
            //如果需要生成动态的域，只需要将该域存入到一个Map<String,Object>对象中即可，该Map<String,Object>的key会生成一个域，域的名字为该Map的key
            //当前Map<String,Object>后面Object的值会作为当前Sku对象该域(key)对应的值
            skuInfo.setSpecMap(specMap);
        }

        //调用dao实现数据批量导入
        skuESMapper.saveAll(skuInfoList);
    }
}
