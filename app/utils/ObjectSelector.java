package utils;

/**
 * Created by sivanookala on 21/10/16.
 */
public interface ObjectSelector<T> {

    boolean isValid(T t);
}