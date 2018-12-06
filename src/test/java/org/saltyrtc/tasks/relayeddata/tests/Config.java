/*
 * Copyright (c) 2018 Threema GmbH
 *
 * Licensed under the Apache License, Version 2.0, <see LICENSE-APACHE file>
 * or the MIT license <see LICENSE-MIT file>, at your option. This file may not be
 * copied, modified, or distributed except according to those terms.
 */

package org.saltyrtc.tasks.relayeddata.tests;

/**
 * Test configuration.
 */
public class Config {
    public static String SALTYRTC_HOST = "localhost";
    public static int SALTYRTC_PORT = 8765;
    public static boolean IGNORE_JKS = false;
    public static String SALTYRTC_SERVER_PUBLIC_KEY = "f77fe623b6977d470ac8c7bf7011c4ad08a1d126896795db9d2b4b7a49ae1045 ";
    // Show debug output
    public static boolean DEBUG = true;
    // Show verbose output, e.g. websocket frames
    public static boolean VERBOSE = false;
}
