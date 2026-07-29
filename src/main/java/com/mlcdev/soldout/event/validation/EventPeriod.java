package com.mlcdev.soldout.event.validation;

import java.time.Instant;


public interface EventPeriod {

    Instant startsAt();

    Instant endsAt();

}
