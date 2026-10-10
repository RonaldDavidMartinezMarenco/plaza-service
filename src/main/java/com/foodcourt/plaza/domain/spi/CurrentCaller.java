package com.foodcourt.plaza.domain.spi;

import com.foodcourt.plaza.domain.model.Caller;

/** Reads the authenticated caller. The domain does not import a security framework. */
public interface CurrentCaller {

    Caller require();
}
