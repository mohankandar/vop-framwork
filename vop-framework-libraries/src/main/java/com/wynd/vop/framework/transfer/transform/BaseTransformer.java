package com.wynd.vop.framework.transfer.transform;

import org.springframework.core.convert.converter.Converter;

/**
 * This abstract class performs two functions related to VOP object conversion / transformation:
 * <ol>
 * <li>Declare the generic requirement for conversion
 * <li>Provide static methods that are commonly needed for object model transformation.
 * </ol>
 *
 * @param <S> the source object to be converted
 * @param <T> the target object to be created and returned

 */
interface BaseTransformer<S, T> extends Converter<S, T> {
}
