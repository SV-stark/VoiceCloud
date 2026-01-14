package com.audiobook.vc.core.featureflag

interface FeatureFlag<T> {
  fun get(): T
}
