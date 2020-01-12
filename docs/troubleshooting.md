---
id: troubleshooting
title: Troubleshooting
---

## Invalid test class

If you define a test suite as an `object` instead of `class` you get the
following error:

```sh
==> X munit.BasicSuite.initializationError  0.003s org.junit.runners.model.InvalidTestClassError: Invalid test class 'munit.BasicSuite':
  1. Test class should have exactly one public constructor
  2. No runnable methods
```

To fix the problem, use `class` instead of `object`

```diff
- object MySuite extends munit.FunSuite { ... }
+ class MySuite extends munit.FunSuite { ... }
```
