package com.geirsson.junit

/** Scala.js internal JUnit test metadata
 *
 *  This class is public due to implementation details. Only the junit compiler
 *  plugin may create instances of it.
 *
 *  Relying on this class directly is unspecified behavior.
 */
final class TestMetadata(
    val name: String,
    val ignored: Boolean,
    val annotation: org.junit.Test
)
