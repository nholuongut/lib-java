//
//  Author: Nho Luong
//  Date: 2014-09-15 20:49:22 +0100 (Mon, 15 Sep 2014)
//
//  https://github.com/nholuongut/lib-java
//
//  License: see accompanying Nho Luong LICENSE file
//
//  If you"re using my code you"re welcome to connect with me on LinkedIn and optionally send me feedback
//  to help improve or steer this or other code I publish
//
//  https://www.linkedin.com/in/nholuong
//

package com.linkedin.nholuongut;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import java.util.HashMap;

public class CLITest {

    private CLI cli;
    private HashMap myDict = new HashMap<String, Integer>();

    class SubCLI extends CLI {
        @Override
        public void run() {
            System.out.println("running SubCLI()");
        }
    }

    @Before
    public void setUp() {
        myDict.put("one", 1);
        myDict.put("two", 2);
        myDict.put("three", 3);
        cli = new SubCLI();
    }

//    set_default_port(80)
    // try {
    //     cli.set_default_port("a")
    //     throw new  Exception("failed to throw CodingError when sending invalid port to set_default_port()")
    // } catch( CodingError {
    //     pass

//        public void test_add_hostoption() {
//            cli.add_hostoption(name = "Ambari", default_host = "localhost", default_port = 8080);
//        }
//
//        public void test_add_useroption() {
//            cli.add_useroption(name = "Ambari", default_user = "admin", default_password = "mysecret");
//        }

//        public void test_add_hostoption_dup_Exception() {
//            try {
//                cli.add_hostoption();
//                cli.add_hostoption();
//                throw new RuntimeException("failed to throw IllegalStateException when duplicating add_hostoption()");
//            } catch (IllegalStateException e) {
//                 pass
//            }
//        }

//    public void test_add_hostoption_port_error_Exception() {
//        try {
//            cli.add_hostoption(default_port = "error");
//            throw new RuntimeException("failed to throw CodingError when sending invalid port to add_hostoption");
//        } catch (IllegalArgumentException e) {
//            pass
//        }
//    }
//
//    public void test_useroption_dup_Exception() {
//        try {
//            cli.add_useroption();
//            cli.add_useroption();
//            throw new RuntimeException("failed to throw OptionConflictError from optparse OptionParser when duplicating add_useroption");
//        } catch (IllegalStateException e) {
//             pass
//        }
//    }

    @Test
    // setVerbose() won't work because parse args resets it so we change verbose_default instead
    public void testVerboseDefaultSettersGetters() {
        cli.setVerboseDefault(2);
        assertEquals("cli.getVerboseDefault", 2, cli.getVerboseDefault());
//        cli.main();
    }

    @Test
    public void testReinitSetVerbose() {
        cli.setVerbose(0);
        assertEquals("cli.getVerbose()", 0, cli.getVerbose());
    }

//        void test_usage() {
//            cli.__init__();
//            try {
//                cli.usage();
//                throw new Exception("failed to exit on CLI.usage()");
//            } catch(IllegalArgumentException e) {
//                if(e.code != 3){
//                    throw new Exception("wrong exit code %s != 3 when exiting usage() from base class CLI" % _.code)
//                }
//            }
//        }

//    @Test(expected=IllegalArgumentException.class)
//    public void test_usage_message() {
//        cli.usage("test message");
//    }

    //void test_parser_version() {
    //    print("parser version = %s" % cli.__parser.get_version())
    // I don"t populate version in OptionParser now as it creates the switch too high in the option order
    // assertTrue(re.search(" version (? {None|%(version_regex)s), CLI version %(version_regex)s, " +
    //                           "Utils version %(version_regex)s" % globals(), cli.__parser.get_version()))

    @Test(expected=IllegalArgumentException.class)
    public void testSetTimeoutMaxSetTimeoutException() {
        cli.setTimeoutMax(5);
        cli.setTimeout(6);
    }

//    @Test
//    public void test_set_timeout_default() {
//        cli = SubCLI();
//        cli.setTimeoutMax(null);
//        cli.setTimeoutDefault(999999);
//        cli.timeout_default(9);
//        assertEquals("cli.setTimeoutDefault(9)", 9, cli.setTimeoutDefault(9));
//        cli.setTimeoutDefault(null);
//    }

    @Test
    public void testSetTimeoutDefault() {
        cli.setTimeoutDefault(9);
        assertEquals(9, cli.getTimeoutDefault());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetTimeoutMaxSetTimeoutDefaultException() {
        cli.setTimeoutMax(10);
        cli.setTimeoutDefault(11);
    }

//    @Test
//    public void test_timeout_default_max_normal() {
//        cli = SubCLI();
//        cli.setTimeoutDefault(null);
//        assertEquals(cli.getTimeoutDefault, null);
//        cli.setTimeoutMax(30);
//        cli.setTimeout(22);
//        assertEquals(22, cli.getTimeout);
//        cli.main();
//    }

//    public void test_timeout_default_sleep_Exception() {
//        cli.setTimeoutDefault(1);
//            cli.run = lambda { time.sleep(3);
//        try {
//            cli.main();
//            throw new RuntimeException("failed to self-timeout after 1 second");
//        } catch(IllegalArgumentException e){
//               if(e.code != 3){
//                    throw new RuntimeException("wrong exit code != 3 when self timing out CLI");
//                }
//        }
//    }
}
