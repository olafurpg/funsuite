package org.junit.runner.notification

import org.junit.runner.Description

class Failure(val description: Description, val ex: Throwable)
