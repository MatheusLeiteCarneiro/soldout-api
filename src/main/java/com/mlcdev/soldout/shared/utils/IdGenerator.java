package com.mlcdev.soldout.shared.utils;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

public final class IdGenerator {

    private IdGenerator(){}

    public static UUID newId(){
        return UuidCreator.getTimeOrderedEpoch();
    }
}
