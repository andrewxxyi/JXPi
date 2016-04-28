package cn.ijingxi.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 0是默认
 *
 * Created by andrew on 15-11-4.
 */
public class jxEnum {
    private static Map<String,Map<String,Integer>> allEnum_S=new HashMap<>();
    private static Map<String,Map<Integer,String>> allEnum_I=new HashMap<>();

    public static void addEnum(String enumName,String purpose){
        Map<String, Integer> map1 = allEnum_S.get(enumName);
        Map<Integer, String> map2=null;
        if(map1==null){
            map1=new HashMap<>();
            allEnum_S.put(enumName,map1);
            map2=new HashMap<>();
            allEnum_I.put(enumName,map2);
        }
        else
            map2=allEnum_I.get(enumName);
        int num=map1.size();
        map1.put(purpose,num);
        map2.put(num,purpose);
    }

    public static int getEnumOrder(String enumName,String purpose) throws Exception {
        Map<String, Integer> map1 = allEnum_S.get(enumName);
        utils.Check(map1==null,"枚举类型不存在："+enumName);
        return map1.get(purpose);
    }

    public static String getEnumName(String enumName,int order) throws Exception {
        Map<Integer, String> map1 = allEnum_I.get(enumName);
        utils.Check(map1==null,"枚举类型不存在："+enumName);
        utils.Check(!map1.containsKey(order),"枚举数不存在："+enumName+":"+order);
        return map1.get(order);
    }

}
