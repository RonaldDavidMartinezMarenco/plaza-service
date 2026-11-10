package com.foodcourt.plaza.domain.spi;

/** Generates a six-digit PIN. The clear value is not stored. */
public interface PinGenerator {

    String generate();
}
