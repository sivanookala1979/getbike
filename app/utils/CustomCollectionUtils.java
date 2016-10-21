package utils;

import java.util.List;

/**
 * Created by sivanookala on 21/10/16.
 */
public class CustomCollectionUtils {

    public static <T> T first(List<T> list)
    {
        if(list.size() > 0)
        {
            return list.get(0);
        }
        return null;
    }
}
