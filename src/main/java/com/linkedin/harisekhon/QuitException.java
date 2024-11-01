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

class QuitException extends RuntimeException {

    public String status = "UNKNOWN";
    public String message = "<no message given>";

    public QuitException() {
        super();
    }

    public QuitException (String message) {
        super(message);
        this.status = "CRITICAL";
        this.message = message;
    }

    public QuitException (Throwable cause) {
        super(cause);
        this.status = "CRITICAL";
    }

    public QuitException(String message, Throwable cause) {
        super(message, cause);
        this.status = "CRITICAL";
        this.message = message;
    }

    //    1.7+, breaks openjdk6 CI tests
//    public QuitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
//        super(message, cause, enableSuppression, writableStackTrace);
//        this.status = "CRITICAL";
//        this.message = message;
//    }

    public QuitException (String status, String message) {
        super(message);
        this.status  = status;
        this.message = message;
    }

}
