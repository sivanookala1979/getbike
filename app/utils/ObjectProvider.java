package utils;

/**
 * Created by sivanookala on 21/10/16.
 */
public interface ObjectProvider<T, U> {

    U getObject(T t);
}
