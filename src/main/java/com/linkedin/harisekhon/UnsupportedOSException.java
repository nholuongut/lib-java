//
//  Author: Nho Luong
//  Date: 20/11/2015
//
//  vim:ts=4:sts=4:sw=4:noet
//
//  https://github.com/nholuongut/lib-java
//
//  License: see accompanying Nho Luong LICENSE file
//
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help improve or steer this or other code I publish
//
//  https://www.linkedin.com/in/nholuong
//

package com.linkedin.nholuongut;

class UnsupportedOSException extends RuntimeException {

    private static String message = "this program is only supported on %s at this time";
    private static String message2 = "this program is not supported on this operating system at this time";

    public UnsupportedOSException() {
        super(message2);
    }

    public UnsupportedOSException(String supportedOSs) {
        super(String.format(message, supportedOSs));
    }

    public UnsupportedOSException(Throwable cause) {
        super(cause);
    }

    public UnsupportedOSException(String supportedOSs, Throwable cause) {
        super(String.format(message, supportedOSs), cause);
    }

//    1.7+, breaks openjdk6 CI tests
//    public UnsupportedOSException(String supportedOSs, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
//        super(String.format(message, supportedOSs), cause, enableSuppression, writableStackTrace);
//    }

//    public UnsupportedOSException(String status, String message) {
//        super(message);
//    }

//    no time to call in super()
//    public String formatMsg(String supportedOSs){
//        return message.format(supportedOSs);
//    }

}
