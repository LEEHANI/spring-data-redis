package com.example.redis;

import java.text.MessageFormat;

public class RedisCacheNameConst {
  public static final String MEMBER_CACHE = "member";
  public static final String MEMBER_LOCK_CACHE = "member:lock:{0}";

  public static String resolveKey(String keyPrefix, String ... identifiers) {
    return MessageFormat.format(keyPrefix, identifiers);
  }

}
