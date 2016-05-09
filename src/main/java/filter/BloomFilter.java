package filter;

import java.util.BitSet;

/**
 * Created by Cabeza on 2016/4/28.
 */
public class BloomFilter {

    private static final int DEFAULT_SIZE = 2 << 24;//比特长度
    private static final int[] seeds = {3,5,7, 11, 13, 31, 37, 61};
    private static BitSet bits = new BitSet(DEFAULT_SIZE);
    private static SimpleHash[] func = new SimpleHash[seeds.length];

    public static void add(String value)
    {
        if(value != null){
            for(SimpleHash f : func)
                bits.set(f.hash(value),true);
        }
    }

    public static boolean contains(String value)
    {
        if(value == null) return false;
        boolean ret = true;
        for(SimpleHash f : func){
            if(!bits.get(f.hash(value)))
                return false;
        }
        return true;
    }
}

class SimpleHash {

    private int cap;
    private int seed;

    public  SimpleHash(int cap, int seed) {
        this.cap = cap;
        this.seed = seed;
    }

    public int hash(String value) {
        int result = 0;
        int len = value.length();
        for (int i = 0; i < len; i++) {
            result = seed * result + value.charAt(i);
        }
        return (cap - 1) & result;
    }
}