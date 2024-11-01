//
//  Author: Nho Luong
//  Date: 2015-05-31 14:05:14 +0100 (Sun, 31 May 2015)
//
//  vim:ts=4:sts=4:sw=4:et
//
//  https://github.com/nholuongut/lib-java
//
//  License: see accompanying Nho Luong LICENSE file
//
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback
//  to help improve or steer this or other code I publish
//
//  https://www.linkedin.com/in/nholuong
//

// my linkedin account is unique and will outlast my personal domains

package com.linkedin.nholuongut;

import static com.linkedin.nholuongut.Utils.*;

import java.io.*;
import java.net.URL;
import java.net.UnknownHostException;
//import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Collections;
import java.util.HashSet;
import java.util.List;

// JUnit 3
//import junit.framework.Test;
//import junit.framework.TestCase;
//import junit.framework.TestSuite;
// JUnit 4
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static java.lang.Math.pow;

/**
 * Unit tests for com.linkedin.nholuongut.Utils
 */
public class UtilsTest { // extends TestCase { // JUnit 3
    /**
     * Create the test case
     *
     * @param testName name of the test case
     *
    public UtilsTest( String testName )
    {
        super( testName );
    }
    */

    /**
     * @return the suite of tests being tested
     *
    public static Test suite()
    {
        return new TestSuite( UtilsTest.class );
    }
    */

    // no method called should take 1 sec, but occasionally this causes build failures on slower systems like Travis VMs
    // or Docker containers so setting a little higher to 3 secs
    @Rule
    public Timeout globalTimeout = Timeout.seconds(3);
    // specified in millis - this should never take even 1 second
    //@Test(timeout=1000)
    // done at global level above now

    // not really designed to be instantiated since there's no state but anyway
    @Test
    public void testUtilsInstance(){
        Utils u = new com.linkedin.nholuongut.Utils();
        assertTrue(u instanceof Utils);
    }

    @Test
    public void testGetStatusCode(){
        // programs depends on this for interoperability with Nagios compatiable monitoring systems
        assertSame("getStatus(OK)",             0,  getStatusCode("OK"));
        assertSame("getStatus(WARNING)",        1,  getStatusCode("WARNING"));
        assertSame("getStatus(CRITICAL)",       2,  getStatusCode("CRITICAL"));
        assertSame("getStatus(UNKNOWN)",        3,  getStatusCode("UNKNOWN"));
        assertSame("getStatus(DEPENDENT)",      4,  getStatusCode("DEPENDENT"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetStatusCodeException(){
        getStatusCode("somethingInvalid");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetStatusException(){
        setStatus("somethingInvalid");
    }

    @Test
    public void testSetStatusGetStatusAndSetShortcuts(){
        // start unknown - but when repeatedly testing this breaks so reset to UNKNOWN at end
        assertTrue(isUnknown());
        assertTrue(getStatus().equals("UNKNOWN"));
        assertEquals(getStatusCode(), 3);
        warning();
        status();
        status2();
        status3();
        assertTrue(isWarning());
        assertTrue(getStatus().equals("WARNING"));

        // shouldn't change from warning to unknown
        unknown();
        assertTrue(isWarning());
        assertTrue(getStatus().equals("WARNING"));
        assertEquals(getStatusCode(), 1);

        // critical should override unknown
        setStatus("OK");
        assertTrue(isOk());
        unknown();
        assertTrue(isUnknown());
        critical();
        assertTrue(isCritical());
        assertTrue(getStatus().equals("CRITICAL"));
        assertEquals(getStatusCode(), 2);

        // critical should override warning
        setStatus("WARNING");
        assertTrue(isWarning());
        critical();
        assertTrue(isCritical());
        unknown(); // shouldn't change critical
        assertTrue(isCritical());
        assertTrue(getStatus().equals("CRITICAL"));

        setStatus("UNKNOWN");
    }

    // JUnit 4.9+ requires System Rules for system exit checks
//    @Rule
//    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

//    @Test
//    public void test_quit_status_msg() {
//        exit.expectSystemExit();
//        quit("CRITICAL", "test");
//    }
//
//    @Test
//    public void test_quit_msg() {
//        exit.expectSystemExit();
//        quit("test");
//    }

    @Test
    public void testPrintln() {
        println("test");
        println(1.0);
        println(1L);
        println(true);
    }

    @Test(expected=QuitException.class)
    public void testQuitException(){
        try {
            quit("CRITICAL", "blah");
        } catch (QuitException e){
            assert "CRITICAL".equals(e.status);
            assert "blah".equals(e.message);
            throw e;
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testQuitInvalidStatusException(){
        quit("INVALID_STATUS", "blah");
    }

//    @Test
//    public void test_HostOptions(){
//        HostOptions();
//    }

    @Test
    public void testLoadTldsNodups() throws IOException {
        tlds.clear();
        loadTlds("tlds-alpha-by-domain.txt");
        loadTlds("tlds-alpha-by-domain.txt");
        assertTrue(tlds.size() > 1000);
        assertTrue(tlds.size() < 2000);
    }

    @Test
    public void testLoadTldsSkip() throws IOException, IllegalStateException {
        int tlds_starting_size = tlds.size();
        String filename = "faketld.txt";
//        URL url = com.linkedin.nholuongut.Utils.class.getResource("/tlds-alpha-by-domain.txt");
        URL url = com.linkedin.nholuongut.Utils.class.getResource("/");
        if(url == null){
            throw new IOException("can't get resource directory path!");
        }
//        String resourcePath = new File(url.getFile()).getParent();
//        File f = new File(String.format("%s/%s". resourcePath, filename));
        File f = new File(String.format("%s/%s", url.getFile(), filename));
        FileWriter fw = new FileWriter(f);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("=");
        bw.close();
        loadTlds(filename);
        if(tlds.contains("=")){
            throw new IllegalStateException("tlds contain '=' which should have been excluded by loadTlds()");
        }
        f.delete();
        assertEquals(tlds_starting_size, tlds.size());
    }

    @Test(expected=IOException.class)
    public void testLoadTldsNonexistent() throws Exception {
        loadTlds("nonexistentfile.txt");
        // shouldn't reach here
        throw new Exception("loadTlds() failed to thrown an IOException for a nonexistent file");
    }

    @Test(expected=IllegalStateException.class)
    public void testCheckTldcountTooHigh() throws IOException {
        loadTlds("tlds-alpha-by-domain.txt");
        for(int i=0; i<1000; i++){
            tlds.add(String.format("%d", i));
        }
        checkTldCount();
    }

    @Test(expected = IllegalStateException.class)
    public void testCheckTldcountTooLow(){
        tlds.clear();
        checkTldCount();
    }

    // ====================================================================== //
    // tests both array to arraylist at same time
    @Test
    public void testArrayToArraylist(){
        String[] a = {"node1:9200","node2","node3:8080","node4","node5"};
        assertArrayEquals("arrayToArraylist()", a, arraylistToArray(arrayToArraylist(a)));
    }

    @Test
    public void testSetToArray(){
        String[] a = {"test"};
        HashSet<String> b = new HashSet<String>();
        b.add("test");
        assertArrayEquals("setToArray()", a, setToArray(b));
    }

    // ====================================================================== //

    @Test
    public void testName(){
        assertEquals("name(null)", "", name(null));
        assertEquals("name(blank)", "", name(""));
        assertEquals("name(blah)", "blah ", name("blah"));
    }

    @Test
    public void testRequireName(){
        assertEquals("requireName(blah)", "blah", requireName("blah"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRequireNameExceptionNull() throws IllegalArgumentException {
        assertEquals("requireName(null)", "", requireName(null));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRequireNameExceptionBlank() throws IllegalArgumentException {
        assertEquals("requireName(blank)", "", requireName(""));
    }

//    @Test(expected=IllegalArgumentException.class)
//    public void test_arg_error() throws IllegalArgumentException {
//        arg_error("blah");
//    }
//
//    @Test(expected=IllegalStateException.class)
//    public void test_state_error() throws IllegalStateException {
//        state_error("blah");
//    }

    // ====================================================================== //
    @Test
    public void testCheckRegex(){
        //println(checkRegex("test", "test"));
        assertTrue("checkRegex(test,test)",   checkRegex("test", "test"));
        assertTrue("checkRegex(test,test)",   checkRegex("test", "te.t"));
        assertFalse("checkRegex(test,test2)", checkRegex("test", "^est"));
        assertFalse("checkRegex(null,test)",  checkRegex(null, "test"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCheckRegexException() throws IllegalArgumentException {
        checkRegex("test", "*est");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCheckRegexNullRegexException() throws IllegalArgumentException {
        checkRegex("test", null);
    }

    // ====================================================================== //
    @Test
    public void testCheckString(){
        //println(checkString("test", "test"));
        assertTrue("checkString(test,test)",   checkString("test", "test"));             // will use ==
        assertTrue("checkString(test,test)",   checkString("test", "test", true));
        assertTrue("checkString(test,test)",   checkString("test", new String("test"))); // avoid string pool, check checkString uses .equals()
        assertFalse("checkString(test,test2)", checkString("test", "test2"));
        assertFalse("checkString(null,test2)", checkString(null, "test2"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCheckStringNullException() throws IllegalArgumentException {
        checkString("test", null);
    }

    // ====================================================================== //
//    @Test
//    public void test_get_options() {
//        get_options(new String[]{"-H", "host1.internal", "-P", "80"});
//    }

    // ====================================================================== //
    @Test
    public void testExpandUnits(){
        //println(expandUnits(10, "kb")); // => 10240
        //println(10240L);
        assertEquals("expandUnits(10, KB)",    10240L,             expandUnits(10L, "KB"));
        assertEquals("expandUnits(10, mB)",    10485760,           expandUnits(10, "mB"));
        assertEquals("expandUnits(10, Gb)",    10737418240L,       expandUnits(10L, "Gb"));
        assertEquals("expandUnits(10, tb)",    10995116277760L,    expandUnits(10L, "tb"));
        assertEquals("expandUnits(10, Pb)",    11258999068426240L, expandUnits(10L, "Pb"));
        assertEquals("expandUnits(10, KB, name)",  1024L,          expandUnits(1L, "KB", "name"));
        assertEquals("expandUnits(10, KB, name)",  10240.0,        expandUnits(10.0, "KB", "name"),   0);
        assertEquals("expandUnits(10, KB)",    10240.0,            expandUnits(10.0, "KB"),   0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testExpandUnitsNullException() throws IllegalArgumentException {
        expandUnits(10, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testExpandUnitsInvalidUnitsException() throws IllegalArgumentException {
        expandUnits(10, "Kbps");
    }

    @Test
    public void testPlural(){
        assertEquals("plural(1)", "", plural(1));
        assertEquals("plural('1')", "", plural("1"));
        assertEquals("plural()", "", plural(""));
        assertEquals("plural( )", "", plural(" "));
        assertEquals("plural(null)", "", plural(null));
        assertEquals("plural(0)", "s", plural(0));
        assertEquals("plural('0')", "s", plural("0"));
        assertEquals("plural(2)", "s", plural(2));
        assertEquals("plural(3.0)", "s", plural(3.0));
        assertEquals("plural(4.0)", "s", plural("4.0"));
        // list tests
//        ArrayList a = new ArrayList();
//        assertEquals("plural(arraylist_0)", "s", plural(a));
//        a.add(1);
//        assertEquals("plural(arraylist_1)", "", plural(a));
//        a.add(2);
//        assertEquals("plural(arraylist_2)", "s", plural(a));
        // raw array tests
//        String[] b = new String[]{};
//        assertEquals("plural(array_0)", "s", plural(b));
//        b.add(1);
//        assertEquals("plural(array_1)", "", plural(b));
//        b.add(2);
//        assertEquals("plural(array_2)", "s", plural(b));
    }

    // ====================================================================== //
    @Test
    public void testHr(){
        hr();
    }

    // ====================================================================== //
    @Test
    public void testHumanUnits(){
        //println(humanUnits(1023     * pow(1024,1)));
        assertEquals("humanUnits(1023)",   "1023 bytes",   humanUnits(1023));
        assertEquals("human units KB",      "1023KB",       humanUnits(1023 * pow(1024, 1)));
        assertEquals("humanUnits MB",      "1023.1MB",     humanUnits(1023.1 * pow(1024, 2)));
        assertEquals("human units GB",      "1023.2GB",     humanUnits(1023.2 * pow(1024, 3)));
        assertEquals("human units TB",      "1023.31TB",    humanUnits(1023.31 * pow(1024, 4)));
        assertEquals("human units PB",      "1023.01PB",    humanUnits(1023.012 * pow(1024, 5)));
        assertEquals("human units EB",      "1023EB",       humanUnits(1023 * pow(1024, 6)));

        assertEquals("humanUnits(1023)",   "1023 bytes",   humanUnits(1023, "b"));
        assertEquals("humanUnits(1023)",   "1023B",        humanUnits(1023, "b", true));
        assertEquals("human units KB",      "1023KB",       humanUnits(1023, "KB"));
        assertEquals("humanUnits MB",      "1023.1MB",     humanUnits(1023.1, "MB"));
        assertEquals("human units GB",      "1023.2GB",     humanUnits(1023.2, "GB"));
        assertEquals("human units TB",      "1023.31TB",    humanUnits(1023.31, "TB"));
        assertEquals("human units PB",      "1023.01PB",    humanUnits(1023.012, "PB"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testHumanUnitsInvalidUnitsException() throws IllegalArgumentException {
        humanUnits(pow(1024, 7), "");
    }

    // ====================================================================== //
    @Test
    public void testResolveIp() throws UnknownHostException {
        // if not on a decent OS assume I'm somewhere lame like a bank where internal resolvers don't resolve internet addresses
        // this way my continous integration tests still run this one
//        if(isLinuxOrMac()){
            assertEquals("resolveIp(a.resolvers.level3.net)",  "4.2.2.1",  resolveIp("a.resolvers.level3.net"));
            assertEquals("validateResolvable()", "4.2.2.1",  validateResolvable("a.resolvers.level3.net"));
//        }
        assertEquals("resolveIp(4.2.2.1)",    "4.2.2.1",  resolveIp("4.2.2.1"));
        assertEquals("validateResolvable()",  "4.2.2.2",  validateResolvable("4.2.2.2"));
    }

    // Some DNS servers return default answers for anything that doesn't resolve in order to redirect you to a landing page
//    @Test(expected=UnknownHostException.class)
//    public void test_resolve_ip_nonexistenthost_exception() throws UnknownHostException {
//        resolveIp("nonexistenthost.local");
//    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateResolveIpNullException() throws IllegalArgumentException, UnknownHostException {
        resolveIp(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateResolveIpBlankException() throws IllegalArgumentException, UnknownHostException {
        resolveIp(" ");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateResolvableNullException() throws IllegalArgumentException, UnknownHostException {
        validateResolvable(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateResolvableBlankException() throws IllegalArgumentException, UnknownHostException {
        validateResolvable(" ");
    }

    // Some DNS servers return default answers for anything that doesn't resolve in order to redirect you to a landing page
//    @Test(expected=UnknownHostException.class)
//    public void testValidateResolvableBlankException() throws IllegalArgumentException, UnknownHostException {
//        validateResolvable("nonexistenthost.local");
//    }

    // ====================================================================== //
    @Test
    public void testANSIEscapeCodes(){
        assertEquals("stripANSIEscapeCodes(some content here)",         "some content here",            stripANSIEscapeCodes("some \033[01;31m\033[Kcontent\033[m\033[K here"));
        assertEquals("stripANSIEcapeCodes(some highlighted content)",   "some highlighted content",     stripANSIEscapeCodes("some \033[01;31m\033[Khigh\033[m\033[Klighted \033[01;31m\033[Kcontent\033[m\033[K"));
    }

    // ====================================================================== //
    @Test
    public void testStripScheme(){
        assertEquals("stripScheme(file:/blah)",                        "/blah",                        stripScheme("file:/blah"));
        assertEquals("stripScheme(file:///path/to/blah)",              "/path/to/blah",                stripScheme("file:///path/to/blah"));
        assertEquals("stripScheme(http://blah)",                       "blah",                         stripScheme("http://blah"));
        assertEquals("stripScheme(hdfs:///blah)",                      "/blah",                        stripScheme("hdfs:///blah"));
        assertEquals("stripScheme(hdfs://namenode/path/to/blah)",      "namenode/path/to/blah",        stripScheme("hdfs://namenode/path/to/blah"));
        assertEquals("stripScheme(hdfs://namenode:8020/path/to/blah)", "namenode:8020/path/to/blah",   stripScheme("hdfs://namenode:8020/path/to/blah"));
    }

    // ====================================================================== //
    @Test
    public void testStripSchemeHost(){
        assertEquals("stripSchemeHost(file:/blah)",                               "/blah",            stripSchemeHost("file:/blah"));
        assertEquals("stripSchemeHost(file:///path/to/blah)",                     "/path/to/blah",    stripSchemeHost("file:///path/to/blah"));
        assertEquals("stripSchemeHost(hdfs:///path/to/blah)",                     "/path/to/blah",    stripSchemeHost("hdfs:///path/to/blah"));
        assertEquals("stripSchemeHost(http://my.domain.com/blah)",                "/blah",            stripSchemeHost("http://my.domain.com/blah"));
        assertEquals("stripSchemeHost(hdfs://nameservice1/hdfsfile)",             "/hdfsfile",        stripSchemeHost("hdfs://nameservice1/hdfsfile"));
        assertEquals("stripSchemeHost(hdfs://nameservice1:8020/hdfsfile)",        "/hdfsfile",        stripSchemeHost("hdfs://nameservice1:8020/hdfsfile"));
        assertEquals("stripSchemeHost(hdfs://namenode.domain.com/hdfsfile)",      "/hdfsfile",        stripSchemeHost("hdfs://namenode.domain.com/hdfsfile"));
        assertEquals("stripSchemeHost(hdfs://namenode.domain.com:8020/hdfsfile)", "/hdfsfile",        stripSchemeHost("hdfs://namenode.domain.com:8020/hdfsfile"));
        assertEquals("stripSchemeHost(hdfs://nameservice1/path/to/hdfsfile)",     "/path/to/hdfsfile", stripSchemeHost("hdfs://nameservice1/path/to/hdfsfile"));
    }

//    @Test(expected=IllegalArgumentException.class)
//    public void test_usage(){
//        usage("test");
//    }

//    @Test(expected=IllegalArgumentException.class)
//    public void test_usage_blank(){
//        usage("");
//    }

//    @Test(expected=IllegalArgumentException.class)
//    public void test_usage_empty(){
//        usage();
//    }

    // ====================================================================== //


    @Test
    public void testUniqArray(){
        String[] myArray = {"one","two","three","","one"};
        String[] myArray_deduped = {"one","two","three",""};
        String[] myArray_test = uniqArray(myArray);
        // The ordering is highly dependent on JDK version and fails on Oracle JDK 8 in Travis so must sort the arrays for comparison
        Arrays.sort(myArray_deduped);
        Arrays.sort(myArray_test);
        assertArrayEquals(myArray_deduped, myArray_test);
    }

    @Test
    public void testUniqArraylist(){
        List<String> myList = Arrays.asList("one", "two", "three", "", "one");
        String[] myArray_deduped = {"one","two","three",""};
        String[] myArray_test = arraylistToArray(uniqArraylist(myList));
        // The ordering is highly dependent on JDK version and fails on Oracle JDK 8 in Travis so must sort the arrays for comparison
        Arrays.sort(myArray_deduped);
        Arrays.sort(myArray_test);
//        Collections.sort(myList);
//        String[] myArray_test = arraylistToArray(myList);
        assertArrayEquals(myArray_deduped, myArray_test);
    }

    @Test
    public void testUniqArrayOrdered() {
        assertArrayEquals("uniqArrayOrdered(one,two,three,,one)", new String[]{ "one", "two", "three", ""}, uniqArrayOrdered(new String[]{"one", "two", "three", "", "one"}));
    }

    @Test
    public void testUniqArraylistOrdered(){
        String[] a = {"one","two","three","","one"};
        String[] b = {"one","two","three",""};
        assertArrayEquals("uniqArraylistOrdered(one,two,three,,one)", b, arraylistToArray(uniqArraylistOrdered(arrayToArraylist(a))));
    }

    // ====================================================================== //
    //                          O S   H e l p e r s
    // ====================================================================== //

    /*
    @Test
    public void print_java_properties(){
        print_java_properties();
    }
    */
    //import static org.hamcrest.CoreMatchers.
    @Test
    public void testGetOS(){
        assertTrue("getOS()", getOS().matches(".*(?:Linux|Mac|Windows).*"));
    }

    @Test
    public void testIsOS(){
        assertTrue(isOS(System.getProperty("os.name")));
    }

    @Test
    public void testIsLinux() throws UnsupportedOSException {
        if(isLinux()){
            assertEquals("isLinux()", "Linux", getOS());
            linuxOnly();
        }
    }

    @Test
    public void testIsLinuxException() throws Exception {
        if(!isLinux()){
            try {
                linuxOnly();
                throw new Exception("failed to raise UnsupportedOSException");
            } catch (UnsupportedOSException e){
                // pass
            }
        }
    }

    @Test
    public void testIsMac(){
        if(isMac()){
            assertEquals("isMac()", "Mac OS X", getOS());
            macOnly();
        }
    }

    @Test
    public void testIsMacException() throws Exception {
        if(!isMac()){
            try {
                macOnly();
                throw new Exception("failed to raise UnsupportedOSException");
            } catch (UnsupportedOSException e){
                // pass
            }
        }
    }

    @Test
    public void testIsLinuxOrMac(){
        if(isLinuxOrMac()){
            assertTrue("isLinuxOrMac()", getOS().matches("Linux|Mac OS X"));
            linuxMacOnly();
        }
    }

    @Test
    public void testIsLinuxOrMacException() throws Exception {
        if(!isLinuxOrMac()){
            try {
                linuxMacOnly();
                throw new Exception("failed to raise UnsupportedOSException");
            } catch (UnsupportedOSException e){
                // pass
            }
        }
    }

    // ====================================================================== //
    //             V a l i d a t i o n    M e t h o d s
    // ====================================================================== //

    @Test
    public void testIsAlNum(){
        assertTrue("isAlNum(ABC123efg)", isAlNum("ABC123efg"));
        assertTrue("isAlNum(0)",         isAlNum("0"));
        assertFalse("isAlNum(1.2)",      isAlNum("1.2"));
        assertFalse("isAlNum(\"\")",     isAlNum(""));
        assertFalse("isAlNum(null)",     isAlNum(null));
        assertFalse("isAlNum(hari@domain.com)",     isAlNum("hari@domain.com"));
    }

    @Test
    public void testValidateAlnum(){
        assertEquals("validateAlnum(Alnum2Test99, alnum test)", "Alnum2Test99", validateAlnum("Alnum2Test99", "alnum test"));
        assertEquals("validateAlnum(0, alnum zero)", "0", validateAlnum("0", "alnum zero"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAlnumException(){
        validateAlnum("1.2", "alnum exception");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAlnumNullException(){
        validateAlnum(null, "alnum exception");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAlnumBlankException(){
        validateAlnum(" ", "alnum exception");
    }

    // ====================================================================== //
    @Test
    public void testIsAwsAccessKey(){
        assertTrue(isAwsAccessKey(repeatString("A", 20)));
        assertTrue(isAwsAccessKey(repeatString("1", 20)));
        assertTrue(isAwsAccessKey(repeatString("A1", 10)));
        assertFalse(isAwsAccessKey(repeatString("@", 20)));
        assertFalse(isAwsAccessKey(repeatString("A", 40)));
        assertFalse(isAwsAccessKey(null));
    }

    @Test
    public void testValidateAwsAccessKey(){
        assertEquals("validateAwsAccessKey(A * 20)", repeatString("A", 20), validateAwsAccessKey(repeatString("A", 20)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsAccessKeyException(){
        validateAwsAccessKey(repeatString("A", 21));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsAccessKeyNullException(){
        validateAwsAccessKey(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsAccessKeyBlankException(){
        validateAwsAccessKey(" ");
    }

    // ====================================================================== //

    @Test
    public void testIsAwsBucket(){
        assertTrue("isAwsBucket(BucKeT63)", isAwsBucket("BucKeT63"));
        assertFalse("isAwsBucket(BucKeT63)", isAwsBucket("B@cKeT63"));
        assertFalse("isAwsBucket(null)", isAwsBucket(null));
    }

    @Test
    public void testValidateAwsBucket(){
        assertEquals("validateAwsBucket(BucKeT63)", "BucKeT63", validateAwsBucket("BucKeT63"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsBucketException(){
        validateAwsBucket("B@cKeT63");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsBucketNullException(){
        validateAwsBucket(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsBucketBlankException(){
        validateAwsBucket(" ");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsBucketIpException(){
        validateAwsBucket("4.2.2.1");
    }

    // ====================================================================== //

    @Test
    public void testIsAwsHostname(){
        assertTrue(isAwsHostname("ip-172-31-1-1"));
        assertTrue(isAwsHostname("ip-172-31-1-1.eu-west-1.compute.internal"));
        assertFalse(isAwsHostname("nholuongut"));
        assertFalse(isAwsHostname("10.10.10.1"));
        assertFalse(isAwsHostname(repeatString("A", 40)));
        assertFalse(isAwsHostname(null));
    }

    @Test
    public void testValidateAwsHostname(){
        assertEquals("validateAwsHostname(ip-172-31-1-1)", "ip-172-31-1-1", validateAwsHostname("ip-172-31-1-1"));
        assertEquals("validateAwsHostname(ip-172-31-1-1.eu-west-1.compute.internal)", "ip-172-31-1-1.eu-west-1.compute.internal", validateAwsHostname("ip-172-31-1-1.eu-west-1.compute.internal"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsHostnameException() {
        validateAwsHostname("nholuongut");
    }
    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsHostnameException2() {
        validateAwsHostname("10.10.10.1");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsHostnameException3() {
        validateAwsHostname(repeatString("A", 40));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsHostnameNullException() {
        validateAwsHostname(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsHostnameBlankException() {
        validateAwsHostname(" ");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsHostnameIpException() {
        validateAwsHostname("4.2.2.1");
    }

    // ====================================================================== //

    @Test
    public void testIsAwsFqdn(){
        assertTrue(isAwsFqdn("ip-172-31-1-1.eu-west-1.compute.internal"));
        assertFalse(isAwsFqdn("ip-172-31-1-1"));
        assertFalse(isAwsFqdn(null));
    }

    @Test
    public void testValidateAwsFqdn(){
        assertEquals("validateAwsFqdn(ip-172-31-1-1.eu-west-1.compute.internal)", "ip-172-31-1-1.eu-west-1.compute.internal", validateAwsFqdn("ip-172-31-1-1.eu-west-1.compute.internal"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsFqdnException() {
        validateAwsFqdn("ip-172-31-1-1");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsFqdnNullException() {
        validateAwsFqdn(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsFqdnBlankException() {
        validateAwsFqdn(" ");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsFqdnIpException() {
        validateAwsFqdn("4.2.2.1");
    }

    // ====================================================================== //

    @Test
    public void testIsAwsSecretKey(){
        assertTrue(isAwsSecretKey(repeatString("A", 40)));
        assertTrue(isAwsSecretKey(repeatString("1", 40)));
        assertTrue(isAwsSecretKey(repeatString("A1", 20)));
        assertFalse(isAwsSecretKey(repeatString("@", 40)));
        assertFalse(isAwsSecretKey(repeatString("A", 20)));
        assertFalse(isAwsSecretKey(null));
    }

    @Test
    public void testValidateAwsSecretKey(){
        assertEquals("validateAwsSecretKey(A * 40)", repeatString("A", 40), validateAwsSecretKey(repeatString("A", 40)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsSecretKeyException(){
        validateAwsSecretKey(repeatString("A", 41));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsSecretKeyNullException(){
        validateAwsSecretKey(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateAwsSecretKeyBlankException(){
        validateAwsSecretKey(" ");
    }

    // ====================================================================== //
    @Test
    public void testIsChars(){
        assertTrue(isChars("Alpha-01_", "A-Za-z0-9_-"));
        assertFalse(isChars("Alpha-01_*", "A-Za-z0-9_-"));
        assertFalse(isChars("Alpha-01_*", null));
        assertFalse(isChars(null, "A-Za-z0-9_-"));
        assertFalse(isChars(null, null));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testIsCharsException(){
        isChars("Alpha-01_*", "B-A");
    }

    @Test
    public void testValidateChars(){
        assertEquals("validateChars(...)", "log_date=2015-05-23_10", validateChars("log_date=2015-05-23_10", "validate chars", "A-Za-z0-9_=-"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateCharsException(){
        validateChars("Alpha-01_*", "validate chars", "A-Za-z0-9_-");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateCharsNullException(){
        validateChars("Alpha-01_*", "validate chars", null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateCharsNullArgException(){
        validateChars(null, "validate chars", "A-Za-z0-9_-");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateCharsBlankException(){
        validateChars("Alpha-01_*", "validate chars", " ");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateCharsBlankArgException(){
        validateChars(null, "validate chars", "A-Za-z0-9_-");
    }

    // ====================================================================== //
    @Test
    public void testIsCollection(){
        assertTrue(isCollection("students.grades"));
        assertFalse(isCollection("wrong@.grades"));
        assertFalse(isCollection(null));
    }

    @Test
    public void testValidateCollection(){
        assertEquals("validateCollection(students.grades)", "students.grades", validateCollection("students.grades"));
        assertEquals("validateCollection(students.grades, name)", "students.grades", validateCollection("students.grades", "name"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateCollectionException(){
        validateCollection("wrong@grades", "name");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateCollectionNullException(){
        validateCollection(null, "name");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateCollectionBlankException(){
        validateCollection(" ", "name");
    }

    // ====================================================================== //
    @Test
    public void testIsDatabaseName(){
        assertTrue(isDatabaseName("mysql1"));
        assertFalse(isDatabaseName("my@sql"));
        assertFalse(isDatabaseName(null));
    }

    @Test
    public void testValidateDatabase(){
        assertEquals("validateDatabase(mysql)", "mysql", validateDatabase("mysql"));
        assertEquals("validateDatabase(mysql, MySQL)", "mysql", validateDatabase("mysql", "MySQL"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseException(){
        validateDatabase("my@sql", "name");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseNullException(){
        validateDatabase(null, "name");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseBlankException(){
        validateDatabase(" ", "name");
    }

    // ====================================================================== //
    @Test
    public void testIsDatabaseColumnName(){
        assertTrue(isDatabaseColumnName("myColumn_1"));
        assertFalse(isDatabaseColumnName("'column'"));
        assertFalse(isDatabaseColumnName(null));
    }

    @Test
    public void testValidateDatabaseColumnname(){
        assertEquals("validateDatabaseColumnname(myColumn_1)", "myColumn_1", validateDatabaseColumnname("myColumn_1"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseColumnameException(){
        validateDatabaseColumnname("'column'");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseColumnameNullException(){
        validateDatabaseColumnname(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseColumnameBlankException(){
        validateDatabaseColumnname(" ");
    }

    // ====================================================================== //
    @Test
    public void testIsDatabaseFieldName(){
        assertTrue(isDatabaseFieldName("2"));
        assertTrue(isDatabaseFieldName("age"));
        assertTrue(isDatabaseFieldName("count(*)"));
        assertFalse(isDatabaseFieldName("@something"));
        assertFalse(isDatabaseFieldName(null));
    }

    @Test
    public void testValidateDatabaseFieldname(){
        assertEquals("validateDatabaseFieldname(age)", "age", validateDatabaseFieldname("age"));
        assertEquals("validateDatabaseFieldname(10)", "10", validateDatabaseFieldname("10"));
        assertEquals("validateDatabaseFieldname(count(*))", "count(*)", validateDatabaseFieldname("count(*)"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseFieldnameException(){
        validateDatabaseFieldname("@something");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseFieldnameNullException(){
        validateDatabaseFieldname(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseFieldnameBlankException(){
        validateDatabaseFieldname(" ");
    }

    // ====================================================================== //
    @Test
    public void testIsDatabaseTableName(){
        assertTrue(isDatabaseTableName("myTable_1"));
        assertTrue(isDatabaseTableName("default.myTable_1", true));
        assertFalse(isDatabaseTableName("'table'"));
        assertFalse(isDatabaseTableName("default.myTable_1", false));
        assertFalse(isDatabaseTableName("default.myTable_1"));
        assertFalse(isDatabaseTableName(null));
    }

    @Test
    public void testValidateDatabaseTablename(){
        assertEquals("validateDatabaseTablename(myTable)", "myTable", validateDatabaseTablename("myTable"));
        assertEquals("validateDatabaseTablename(myTable, Hive)", "myTable", validateDatabaseTablename("myTable", "Hive"));
        assertEquals("validateDatabaseTablename(myTable, Hive)", "myTable", validateDatabaseTablename("myTable", false));
        assertEquals("validateDatabaseTablename(default.myTable, Hive, true)", "default.myTable", validateDatabaseTablename("default.myTable", "Hive", true));
        assertEquals("validateDatabaseTablename(default.myTable, true)", "default.myTable", validateDatabaseTablename("default.myTable", true));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseTablenameException(){
        validateDatabaseTablename("default.myTable");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseTablenameQualifiedException(){
        validateDatabaseTablename("default.myTable", false);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseTablenameNullException(){
        validateDatabaseTablename(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseTablenameBlankException(){
        validateDatabaseTablename(" ");
    }

    // ====================================================================== //
    @Test
    public void testIsDatabaseViewName(){
        assertTrue(isDatabaseViewName("myView_1"));
        assertTrue(isDatabaseViewName("default.myView_1", true));
        assertFalse(isDatabaseViewName("'view'"));
        assertFalse(isDatabaseViewName("default.myView_1", false));
        assertFalse(isDatabaseViewName("default.myView_1"));
    }

    @Test
    public void testValidateDatabaseViewname(){
        assertEquals("validateDatabaseViewname(myView)", "myView", validateDatabaseViewname("myView"));
        assertEquals("validateDatabaseViewname(myView, Hive)", "myView", validateDatabaseViewname("myView", "Hive"));
        assertEquals("validateDatabaseViewname(default.myView, Hive, true)", "default.myView", validateDatabaseViewname("default.myView", "Hive", true));
        assertEquals("validateDatabaseViewname(default.myView, Hive, true)", "default.myView", validateDatabaseViewname("default.myView", true));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseViewnameException(){
        validateDatabaseViewname("default.myView");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseViewnameNullException(){
        validateDatabaseViewname(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseViewnameBlankException(){
        validateDatabaseViewname(" ");
    }

    // ====================================================================== //

    @Test
    public void testIsDirname(){
        assertTrue("isDirname(test_Dir)", isDirname("test_Dir"));
        assertTrue("isDirname(/tmp/test)", isDirname("/tmp/test"));
        assertTrue("isDirname(./test)", isDirname("./test"));
        assertFalse("isDirname(@me)", isDirname("@me"));
        assertFalse("isDirname(@me)", isDirname(null));
    }

    @Test
    public void testValidateDirname(){
        assertEquals("validateDirname(./src)",     "./src",    validateDirname("./src", "dirname"));
        assertEquals("validateDirname(./src, true)",   "./src",    validateDirname("./src", "dirname", true));
        assertEquals("validateDirname(/etc)",  "/etc",     validateDirname("/etc", "dirname"));
        assertEquals("validateDirname(/etc/)",     "/etc/",    validateDirname("/etc/", "dirname"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDirnameException(){
        validateDirname("b@dDir");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDirnameNullException(){
        validateDirname(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDirnameBlankException(){
        validateDirname(" ");
    }

    @Test
    public void testValidateDirectory(){
        if(isLinuxOrMac()){
            assertEquals("validateDirectory(./src)",   "./src",    validateDirectory("./src", "directory"));
            assertEquals("validateDirectory(./src, true)",     "./src",    validateDirectory("./src", "directory", true));
            assertEquals("validateDirectory(/etc)",    "/etc",     validateDirectory("/etc", "directory"));
            assertEquals("validateDirectory(/etc/)",   "/etc/",    validateDirectory("/etc/", "directory"));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDirectoryException(){
        validateDirectory("b@dDir");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDirectoryNullException(){
        validateDirectory(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDirectoryBlankException(){
        validateDirectory(" ");
    }
    @Test(expected=IllegalArgumentException.class)
    public void testValidateDirectoryNonexistentException(){
        validateDirectory("/etc/nonexistent");
    }

    @Test
    public void testValidateDir(){
        if(isLinuxOrMac()){
            assertEquals("validateDir(./src)",     "./src",    validateDir("./src", "directory"));
            assertEquals("validateDir(./src, true)",   "./src",    validateDir("./src", "directory", true));
            assertEquals("validateDir(/etc)",      "/etc",     validateDir("/etc", "dir"));
            assertEquals("validateDir(/etc/)",     "/etc/",    validateDir("/etc/", "dir"));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDirException(){
        validateDir("b@dDir");
    }

    // ====================================================================== //
    @Test
    public void testValidateDouble() {
        validateDouble(2.0, "two", 2, 3);
        validateDouble(3.0, "three", 2, 3);
        assertEquals(2.1, validateDouble("2.1", "two string", 2, 3), 0);
    }

    @Test
    public void testValidateFloat() {
        validateFloat(2.0f, "two", 2f, 3f);
        assertEquals(2f, validateFloat("2.0", "two string", 2f, 3f), 0);
    }

    @Test
    public void testValidateLong() {
        validateLong(2L, "two", 2L, 3L);
        assertEquals(2L, validateLong("2", "two string", 2L, 3L));
    }

    @Test
    public void testValidateInt() {
        validateInt(2, "two", 2, 3);
        assertEquals(2, validateInt("2", "two string", 2, 3));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDoubleException(){
        validateDouble("a", "non-double", 2, 3);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDoubleLowerException(){
        validateDouble(2.0, "name", 3, 4);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDoubleHigherException(){
        validateDouble(4.0, "name", 2, 3);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDoubleInvertedThresholdsException(){
        validateDouble(2.0, "name", 3, 2);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateFloatException(){
        validateFloat("a", "non-float", 2f, 3f);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateLongException(){
        validateLong("a", "non-long", 2L, 3L);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateIntException(){
        validateInt("a", "non-int", 2, 3);
    }

    // ====================================================================== //
    @Test
    public void testIsDomain(){
        assertTrue(isDomain("localDomain"));
        assertTrue(isDomain("nholuongut.com"));
        assertTrue(isDomain("1nholuongut.com"));
        assertTrue(isDomain("com"));
        assertTrue(isDomain("compute.internal"));
        assertTrue(isDomain("eu-west-1.compute.internal"));
        assertTrue(isDomain(repeatString("a", 63) + ".com"));
        assertFalse(isDomain(repeatString("a", 64) + ".com"));
        assertFalse(isDomain("nholuongut")); // not a valid TLD
        assertFalse(isDomain(null)); // not a valid TLD
    }

    @Test
    public void testValidateDomain(){
        assertEquals("validateDomain(nholuongut.com)", "nholuongut.com", validateDomain("nholuongut.com"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDomainException() {
        validateDomain(repeatString("a", 64) + ".com");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDomainException2() {
        validateDomain("nholuongut");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDomainNullException() {
        validateDomain(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDomainBlankException() {
        validateDomain(" ");
    }

    // ====================================================================== //
    @Test
    public void testIsDomainStrict(){
        assertFalse(isDomainStrict("com"));
        assertTrue(isDomainStrict("domain.com"));
        assertTrue(isDomainStrict("123domain.com"));
        assertTrue(isDomainStrict("domain1.com"));
        assertTrue(isDomainStrict("domain.local"));
        assertTrue(isDomainStrict("domain.localDomain"));
        assertFalse(isDomainStrict(null));
    }

    @Test
    public void testValidateDomainStrict(){
        assertEquals("validateDomainStrict(nholuongut.com)", "nholuongut.com", validateDomainStrict("nholuongut.com"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDomainStrictException() {
        validateDomainStrict("com");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDomainStrictNullException() {
        validateDomainStrict(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDomainStrictBlankException() {
        validateDomainStrict(" ");
    }

    // ====================================================================== //
    @Test
    public void testIsDnsShortName(){
        assertTrue(isDnsShortName("myHost"));
        assertFalse(isDnsShortName("myHost.domain.com"));
        assertFalse(isDnsShortName(null));
    }

    // ====================================================================== //
    @Test
    public void testIsEmail(){
        assertTrue(isEmail("hari'sekhon@gmail.com"));
        assertTrue(isEmail("hari@LOCALDOMAIN"));
        assertFalse(isEmail("nholuongut"));
        assertFalse(isEmail(null));
    }

    @Test
    public void testValidateEmail(){
        assertEquals("validateEmail(hari@domain.com)", "hari@domain.com", validateEmail("hari@domain.com"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateEmailException() {
        validateEmail("nholuongut");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateEmailNullException() {
        validateEmail(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateEmailBlankException() {
        validateEmail(" ");
    }

    // ====================================================================== //
    @Test
    public void testIsFileName(){
        assertTrue(isFilename("some_File.txt"));
        assertTrue(isFilename("/tmp/test"));
        assertFalse(isFilename("@me"));
        assertFalse(isFilename(null));
    }

    @Test
    public void testValidateFilename(){
        assertEquals("validateFilename(./pom.xml)", "./pom.xml", validateFilename("./pom.xml"));
        assertEquals("validateFilename(/etc/passwd)", "/etc/passwd", validateFilename("/etc/passwd"));
        assertEquals("validateFilename(/etc/passwd, name)", "/etc/passwd", validateFilename("/etc/passwd", "name"));
        assertEquals("validateFilename(/nonexistentfile)", "/nonexistentfile", validateFilename("/nonexistentfile", "nonexistentfile", true));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateFilenameException() {
        validateFilename("@me");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateFilenameNullException() {
        validateFilename(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateFilenameBlankException() {
        validateFilename(" ");
    }

    @Test
    public void testValidateFile(){
        assertEquals("validateFile(./pom.xml)", "./pom.xml", validateFile("./pom.xml"));
        assertEquals("validateFile(./pom.xml)", "./pom.xml", validateFile("./pom.xml", "name"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateFileException() {
        validateFile("/nonexistent");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateFileTrailingslashException() {
        validateFilename("/etc/passwd/", "/etc/passwd/");
    }

    // ====================================================================== //
    @Test
    public void testIsFqdn(){
        assertTrue(isFqdn("hari.sekhon.com"));
        assertFalse(isFqdn("hari@nholuongut.com"));
        assertFalse(isFqdn(null));
    }

    @Test
    public void testValidateFqdn(){
        assertEquals("validateFqdn(www.nholuongut.com)", "www.nholuongut.com", validateFqdn("www.nholuongut.com"));
        // permissive because of short tld style internal domains
        assertEquals("validateFqdn(myhost.local, name)", "myhost.local", validateFqdn("myhost.local", "name"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateFqdnException() {
        validateFqdn("hari@nholuongut.com");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateFqdnNullException() {
        validateFqdn(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateFqdnBlankException() {
        validateFqdn(" ");
    }

    // ====================================================================== //
    @Test
    public void testIsHex(){
        assertTrue(isHex("0xAf09b"));
        assertFalse(isHex("9"));
        assertFalse(isHex("0xhari"));
        assertFalse(isHex(null));
    }

    // ====================================================================== //
    //@Test(timeout=1000)
    @Test
    public void testIsHost(){
        assertTrue(isHost("nholuongut.com"));
        assertTrue(isHost("nholuongut"));
        assertTrue(isHost("10.10.10.1"));
        assertTrue(isHost("10.10.10.10"));
        assertTrue(isHost("10.10.10.100"));
        assertTrue(isHost("10.10.10.0"));
        assertTrue(isHost("10.10.10.255"));
        assertTrue(isHost("ip-172-31-1-1"));
        assertFalse(isHost("10.10.10.256"));
        assertFalse(isHost(repeatString("a", 256)));
        assertFalse(isHost(null));
    }

    @Test
    public void testValidateHost(){
        assertEquals("validateHost(10.10.10.10)", "10.10.10.10", validateHost("10.10.10.10", "name"));
        assertEquals("validateHost(myHost)",      "myHost",      validateHost("myHost"));
        assertEquals("validateHost(myHost.myDomain.com)",  "myHost.myDomain.com",  validateHost("myHost.myDomain.com"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostException() {
        validateHost("10.10.10.256");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostNullException() {
        validateHost(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostBlankException() {
        validateHost(" ");
    }

    @Test
    public void testValidateHosts(){
        String[] a = {"node1:9200","node2:80","node3","node4","node5"};
        String[] b = {"node1:9200","node2:80","node3:8080","node4:8080","node5:8080"};
        assertArrayEquals("validateHosts()", b, validateHosts(a, "8080"));
        assertArrayEquals("validateHosts()", b, validateHosts(a, 8080));
        assertArrayEquals("validateHosts()", b, arraylistToArray(validateHosts(arrayToArraylist(a), "8080")));
        assertArrayEquals("validateHosts()", b, arraylistToArray(validateHosts(arrayToArraylist(a), 8080)));
        assertEquals("validateHosts(myHost)",     "myHost:8080",     validateHosts("myHost", 8080));
        assertEquals("validateHosts(myHost)",     "myHost:8081,myHost2:9200",    validateHosts("myHost,myHost2:9200", 8081));
        assertEquals("validateHosts(myHost.myDomain.com)", "myHost.myDomain.com:8080", validateHosts("myHost.myDomain.com", "8080"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostsException() {
        validateHosts("10.10.10.254", "80800");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostsException2() {
        validateHosts("10.10.10.256", "8080");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostsEmptyarrayException() {
        validateHosts(new String[]{}, 8080);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostsEmptyarraylistException() {
        validateHosts(new ArrayList<String>(), 8080);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostsBlankException() {
        validateHosts(" ", 8080);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostsInvalidPortException() {
        validateHosts("10.10.10.10", 80000);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostsInvalidHostportException() {
        validateHosts("10.10.10.10:80000", 8000);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostsNullException() {
        String s = null;
        validateHosts(s, 8000);
    }

    @Test
    public void testValidateHostport(){
        assertEquals("validateHostPort(10.10.10.10:8080)", "10.10.10.10:8080", validateHostPort("10.10.10.10:8080", "name", true));
        assertEquals("validateHostPort(myHost)",      "myHost",      validateHostPort("myHost"));
        assertEquals("validateHostPort(myHost2)",     "myHost2",     validateHostPort("myHost2", "name2"));
        assertEquals("validateHostPort(myHost.myDomain.com)",  "myHost.myDomain.com",  validateHostPort("myHost.myDomain.com", "fqdn_host", false, true));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostportException() {
        validateHostPort("10.10.10.256:8080");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostportException2() {
        validateHostPort("10.10.10.10:80800");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostportException3() {
        validateHostPort("10.10.10.10:8080:8080");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostportPortRequiredException() {
        validateHostPort("10.10.10.10", "name", true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostportNullException() {
        validateHostPort(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostportBlankException() {
        validateHostPort(" ");
    }

    /*
    @Test
    public void test_validate_host_port_user_password(){
    }
    */

    // ====================================================================== //
    @Test
    public void testIsHostname(){
        assertTrue(isHostname("nholuongut.com"));
        assertTrue(isHostname("nholuongut"));
        assertTrue(isHostname("a"));
        assertTrue(isHostname("nholuongut1.com"));
        assertTrue(isHostname("1"));
        assertTrue(isHostname("1nholuongut.com"));
        assertTrue(isHostname(repeatString("a", 63)));
        assertFalse(isHostname(repeatString("a", 64)));
        assertFalse(isHostname("-help"));
        assertFalse(isHostname("hari~sekhon"));
        assertFalse(isHostname(null));
    }

    @Test
    public void testValidateHostname(){
        assertEquals("validateHostname(myHost)",      "myHost",      validateHostname("myHost", "name"));
        assertEquals("validateHostname(myHost.myDomain.com)",  "myHost.myDomain.com",  validateHostname("myHost.myDomain.com"));
        assertEquals("validateHostname(nholuongut1.com)",  "nholuongut1.com",  validateHostname("nholuongut1.com"));
        assertEquals("validateHostname(repeatString(a))", repeatString("a", 63),  validateHostname(repeatString("a", 63)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostnameException() {
        validateHostname("hari~sekhon");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostnameNullException() {
        validateHostname(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateHostnameBlankException() {
        validateHostname(" ");
    }

    // ====================================================================== //
    @Test
    public void testIsInterface(){
        assertTrue(isInterface("eth0"));
        assertTrue(isInterface("bond3"));
        assertTrue(isInterface("lo"));
        assertTrue(isInterface("docker0"));
        assertTrue(isInterface("vethfa1b2c3"));
        assertFalse(isInterface("vethfa1b2z3"));
        assertFalse(isInterface("b@interface"));
        assertFalse(isInterface(null));
    }

    @Test
    public void testValidateInterface(){
        assertEquals("validateInterface(eth0)",  "eth0",  validateInterface("eth0"));
        assertEquals("validateInterface(bond3)", "bond3", validateInterface("bond3"));
        assertEquals("validateInterface(lo)",    "lo",    validateInterface("lo"));
        assertEquals("validateInterface(docker0)", "docker0", validateInterface("docker0"));
        assertEquals("validateInterface(vethfa1b2c3)",    "vethfa1b2c3",    validateInterface("vethfa1b2c3"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateInterfaceException() {
        validateInterface("hvethfa1b2z3");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateInterfaceNullException() {
        validateInterface(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateInterfaceBlankException() {
        validateInterface(" ");
    }

    // ====================================================================== //
    @Test
    public void testIsIP(){
        assertTrue("isIP(10.10.10.1)",        isIP("10.10.10.1"));
        assertTrue("isIP(10.10.10.10)",       isIP("10.10.10.10"));
        assertTrue("isIP(10.10.10.100)",      isIP("10.10.10.100"));
        assertTrue("isIP(254.0.0.254)",       isIP("254.0.0.254"));
        assertTrue("isIP(255.255.255.254)",   isIP("255.255.255.254"));
        assertTrue("isIP(10.10.10.0)",        isIP("10.10.10.0"));
        assertTrue("isIP(10.10.10.255)",      isIP("10.10.10.255"));
        assertFalse("isIP(10.10.10.256)",     isIP("10.10.10.256"));
        assertFalse("isIP(x.x.x.x)",          isIP("x.x.x.x"));
        assertFalse("isIP(null)",             isIP(null));
    }

    @Test
    public void testValidateIp(){
        assertEquals("validateIP(validateIP(10.10.10.1)",     "10.10.10.1",   validateIP("10.10.10.1", "name"));
        assertEquals("validateIP(validateIP(10.10.10.10)",    "10.10.10.10",  validateIP("10.10.10.10"));
        assertEquals("validateIP(validateIP(10.10.10.100)",   "10.10.10.100", validateIP("10.10.10.100"));
        assertEquals("validateIP(validateIP(10.10.10.254)",   "10.10.10.254", validateIP("10.10.10.254"));
        assertEquals("validateIP(validateIP(10.10.10.255)",   "10.10.10.255", validateIP("10.10.10.255"));
        assertEquals("validateIP(validateIP(254.0.0.254)",    "254.0.0.254",  validateIP("254.0.0.254"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateIpException() {
        validateIP("10.10.10.256");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateIpNullException() {
        validateIP(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateIpBlankException() {
        validateIP(" ");
    }

    // ====================================================================== //
    @Test
    public void testIsKrb5Princ(){
        assertTrue(isKrb5Princ("tgt/HARI.COM@HARI.COM"));
        assertTrue(isKrb5Princ("hari"));
        assertTrue(isKrb5Princ("hari@HARI.COM"));
        assertTrue(isKrb5Princ("hari/my.host.local@HARI.COM"));
        assertTrue(isKrb5Princ("cloudera-scm/admin@REALM.COM"));
        assertTrue(isKrb5Princ("cloudera-scm/admin@SUB.REALM.COM"));
        assertTrue(isKrb5Princ("hari@hari.com"));
        assertFalse(isKrb5Princ("hari$HARI.COM"));
        assertFalse(isKrb5Princ(null));
    }

    @Test
    public void testValidateKrb5Princ(){
        assertEquals("validateKrb5Princ(tgt/HARI.COM@HARI.COM)", "tgt/HARI.COM@HARI.COM", validateKrb5Princ("tgt/HARI.COM@HARI.COM", "name"));
        assertEquals("validateKrb5Princ(hari)", "hari", validateKrb5Princ("hari"));
        assertEquals("validateKrb5Princ(hari@HARI.COM)", "hari@HARI.COM", validateKrb5Princ("hari@HARI.COM"));
        assertEquals("validateKrb5Princ(hari/my.host.local@HARI.COM)", "hari/my.host.local@HARI.COM", validateKrb5Princ("hari/my.host.local@HARI.COM"));
        assertEquals("validateKrb5Princ(cloudera-scm/admin@REALM.COM)", "cloudera-scm/admin@REALM.COM", validateKrb5Princ("cloudera-scm/admin@REALM.COM"));
        assertEquals("validateKrb5Princ(cloudera-scm/admin@SUB.REALM.COM)", "cloudera-scm/admin@SUB.REALM.COM", validateKrb5Princ("cloudera-scm/admin@SUB.REALM.COM"));
        assertEquals("validateKrb5Princ(hari@hari.com)", "hari@hari.com", validateKrb5Princ("hari@hari.com"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateKrb5PrincException() {
        validateKrb5Princ("hari$HARI.COM");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateKrb5PrincNullException() {
        validateKrb5Princ(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateKrb5PrincBlankException() {
        validateKrb5Princ(" ");
    }

    // ====================================================================== //

    @Test
    public void testValidateKrb5Realm(){
        assertEquals("validateKrb5Realm(nholuongut.com)", "nholuongut.com", validateKrb5Realm("nholuongut.com"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateKrb5RealmException() {
        validateKrb5Realm("hari$HARI.COM");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateKrb5RealmNullException() {
        validateKrb5Realm(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateKrb5RealmBlankException() {
        validateKrb5Realm(" ");
    }

    // ====================================================================== //
    @Test
    public void testIsLabel(){
        assertTrue(isLabel("st4ts used_(%%)"));
        assertFalse(isLabel("b@dlabel"));
        assertFalse(isLabel(" "));
        assertFalse(isLabel(null));
    }

    @Test
    public void testValidateLabel(){
        assertEquals("validateLabel(st4ts_used (%%))", "st4ts_used (%%)", validateLabel("st4ts_used (%%)"));
        assertEquals("validateLabel(st4ts_used (%%))", "st4ts_used (%%)", validateLabel("st4ts_used (%%)"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateLabelException() {
        validateLabel("b@dlabel");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateLabelNullException() {
        validateLabel(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateLabelBlankException() {
        validateLabel(" ");
    }

    // ====================================================================== //
    @Test
    public void testIsLdapDn(){
        assertTrue(isLdapDn("uid=hari,ou=users,ou=accounts,dc=local"));
        assertTrue(isLdapDn("cn=hari-supseruser,ou=big-users,ou=accounts,dc=local"));
        assertFalse(isLdapDn("hari@LOCAL"));
    }

    @Test
    public void testValidateLdapDn(){
        assertEquals("validateLdapDn(uid=hari,ou=users,ou=accounts,dc=local)", "uid=hari,ou=users,ou=accounts,dc=local", validateLdapDn("uid=hari,ou=users,ou=accounts,dc=local"));
        assertEquals("validateLdapDn(uid=hari,ou=users,ou=accounts,dc=local, name)", "uid=hari,ou=users,ou=accounts,dc=local", validateLdapDn("uid=hari,ou=users,ou=accounts,dc=local", "name"));
        assertEquals("validateLdapDn(cn=hari-superuser,ou=big-users,ou=accounts,dc=local, name)", "cn=hari-superuser,ou=big-users,ou=accounts,dc=local", validateLdapDn("cn=hari-superuser,ou=big-users,ou=accounts,dc=local"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateLdapDnException() {
        validateLdapDn("hari@LOCAL");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateLdapDnNullException() {
        validateLdapDn(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateLdapDnBlankException() {
        validateLdapDn(" ");
    }

    // ====================================================================== //

    @Test
    public void testIsMinVersion(){
        assertTrue(isMinVersion("1.3.0", 1.3));
        assertTrue(isMinVersion("1.3.0-alpha", 1.3));
        assertTrue(isMinVersion("1.3", 1.3));
        assertTrue(isMinVersion("1.4", 1.3));
        assertTrue(isMinVersion("1.3.1", 1.2));
        assertFalse(isMinVersion("1.3.1", 1.4));
        assertFalse(isMinVersion("1.2.99", 1.3));
        assertFalse(isMinVersion("1.3", null));
        assertFalse(isMinVersion("hari", 1.3));
        assertFalse(isMinVersion(null, 1.3));
    }

    // ====================================================================== //
    @Test
    public void testIsNagiosUnit(){
        assertTrue(isNagiosUnit("s"));
        assertTrue(isNagiosUnit("ms"));
        assertTrue(isNagiosUnit("us"));
        assertTrue(isNagiosUnit("b"));
        assertTrue(isNagiosUnit("Kb"));
        assertTrue(isNagiosUnit("Mb"));
        assertTrue(isNagiosUnit("Gb"));
        assertTrue(isNagiosUnit("Tb"));
        assertTrue(isNagiosUnit("c"));
        assertTrue(isNagiosUnit("%"));
        assertFalse(isNagiosUnit("Kbps"));
        assertFalse(isNagiosUnit(null));
    }

    @Test
    public void testValidateUnits(){
        assertEquals("validateUnits(s)",   "s",    validateUnits("s", "name"));
        assertEquals("validateUnits(ms)",  "ms",   validateUnits("ms"));
        assertEquals("validateUnits(us)",  "us",   validateUnits("us"));
        assertEquals("validateUnits(B)",   "B",    validateUnits("B"));
        assertEquals("validateUnits(KB)",  "KB",   validateUnits("KB"));
        assertEquals("validateUnits(MB)",  "MB",   validateUnits("MB"));
        assertEquals("validateUnits(GB)",  "GB",   validateUnits("GB"));
        assertEquals("validateUnits(TB)",  "TB",   validateUnits("TB"));
        assertEquals("validateUnits(c)",   "c",    validateUnits("c"));
        assertEquals("validateUnits(%%)",  "%",    validateUnits("%"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateUnitsException() {
        validateUnits("Kbps");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateUnitsNullException() {
        validateUnits(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateUnitsBlankException() {
        validateUnits(" ");
    }

    // ====================================================================== //

    @Test
    public void testValidateNodeList(){
        assertEquals("validateNodeList(String)", "node1,node2,node3,node4,node5", validateNodeList("node1 ,node2 node3  node4, node5"));
        String[] a = {"node1","node2","node3","node4","node5"};
        assertArrayEquals("validateNodeList(ArrayList<String>)",  arraylistToArray(new ArrayList<String>(Arrays.asList(a))), arraylistToArray(validateNodeList(arrayToArraylist(a))));
        assertArrayEquals("validateNodeList(String[])",  a, validateNodeList(a));
    }

    @Test
    public void testValidateNodeportList(){
        assertEquals("validateNodePortList(String)", "node1:9200,node2,node3:8080,node4,node5", validateNodePortList("node1:9200 ,node2 node3:8080 node4, node5"));
        String[] a = {"node1:9200","node2","node3:8080","node4","node5"};
        assertArrayEquals("validateNodePortList(ArrayList<String>)", arraylistToArray(new ArrayList<String>(Arrays.asList(a))), arraylistToArray(validateNodePortList(arrayToArraylist(a))));
        assertArrayEquals("validateNodePortList(String[])", a, validateNodePortList(a));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateNodeListException() {
        validateNodeList("bad~host");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateNodeListEmptyException() {
        validateNodeList(new ArrayList<String>());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateNodeListEmptyfinalException() {
        ArrayList<String> a = new ArrayList<String>();
        a.add(" ");
        validateNodeList(a);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateNodeListNullException() {
        String n = null;
        validateNodeList(n);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateNodeListBlankException() {
        validateNodeList(" ");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateNodeportListException() {
        validateNodePortList("bad@host");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateNodeportListEmptyException() {
        ArrayList<String> a = new ArrayList<String>();
        validateNodePortList(a);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateNodeportListNullstringException() {
        String n = null;
        validateNodePortList(n);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateNodeportListEmptystringException() {
        validateNodePortList(" ");
    }

    // ====================================================================== //
    @Test
    public void testIsNoSqlKey(){
        assertTrue(isNoSqlKey("nholuongut:check_riak_write.pl:riak1:1385226607.02182:20abc"));
        assertFalse(isNoSqlKey("nholuongut@check_riak_write.pl"));
        assertFalse(isNoSqlKey(null));
    }

    @Test
    public void testValidateNosqlKey(){
        assertEquals("validateNoSqlKey(nholuongut:check_riak_write.pl:riak1:1385226607.02182:20abc)", "nholuongut:check_riak_write.pl:riak1:1385226607.02182:20abc", validateNoSqlKey("nholuongut:check_riak_write.pl:riak1:1385226607.02182:20abc"));
        assertEquals("validateNoSqlKey(nholuongut:check_riak_write.pl:riak1:1385226607.02182:20abc, name)", "nholuongut:check_riak_write.pl:riak1:1385226607.02182:20abc", validateNoSqlKey("nholuongut:check_riak_write.pl:riak1:1385226607.02182:20abc", "name"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateNosqlKeyException() {
        validateNoSqlKey("nholuongut@check_riak_write.pl");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateNosqlKeyNullException() {
        validateNoSqlKey(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateNosqlKeyBlankException() {
        validateNoSqlKey(" ");
    }

    // ====================================================================== //
    @Test
    public void testIsPort(){
        assertTrue(isPort("80"));
        assertTrue(isPort(80));
        assertTrue(isPort(65535));
        assertFalse(isPort(65536));
        assertFalse(isPort("a"));
        assertFalse(isPort("-1"));
        assertFalse(isPort("0"));
        assertFalse(isPort(-1));
        assertFalse(isPort(0));
        assertFalse(isPort(null));
    }

    @Test
    public void testValidatePort(){
        assertEquals("validatePort(1)",     1,         validatePort(1, "name"));
        assertEquals("validatePort(80)",    80,        validatePort(80));
        assertEquals("validatePort(65535)", 65535,     validatePort(65535));
        assertEquals("validatePort(1)",     "1",       validatePort("1"));
        assertEquals("validatePort(80)",    "80",      validatePort("80"));
        assertEquals("validatePort(65535)", "65535" ,  validatePort("65535"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidatePortException() {
        validatePort(65536);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testParsePortException() {
        parsePort("test");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testParsePortParseException() {
        validatePort("test");
    }

    // ====================================================================== //
    @Test
    public void testIsProcessName(){
        assertTrue(isProcessName("../my_program"));
        assertTrue(isProcessName("ec2-run-instances"));
        assertTrue(isProcessName("sh <defunct>"));
        assertFalse(isProcessName("./b@dfile"));
        assertFalse(isProcessName("[init] 3"));
        assertFalse(isProcessName(null));
    }

    @Test
    public void testValidateProcessName(){
        assertEquals("validateProcessName(../my_program)", "../my_program", validateProcessName("../my_program", "name"));
        assertEquals("validateProcessName(ec2-run-instances)", "ec2-run-instances", validateProcessName("ec2-run-instances"));
        assertEquals("validateProcessName(sh <defunct>)", "sh <defunct>", validateProcessName("sh <defunct>"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateProcessException() {
        validateProcessName("./b@dfile");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateProcessNullException() {
        validateProcessName(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateProcessBlankException() {
        validateProcessName(" ");
    }

    // ====================================================================== //
    @Test
    public void testValidateProgramPath(){
        if(isLinuxOrMac()){
            assertEquals("validateProgramPath()", "/bin/sh", validateProgramPath("/bin/sh", "sh"));
            assertEquals("validateProgramPath()", "/bin/sh", validateProgramPath("/bin/sh", "shell", "sh"));
            validateProgramPath("/bin/sh", "sh", ".*/sh$");
            validateProgramPath("/bin/sh", "sh", "/bin/sh$");
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateProgramPathException() {
        validateProgramPath("/bin/sh-nonexistent", "sh-nonexistent");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateProgramPathDirException() {
        validateProgramPath("/bin", "/bin");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateProgramPathNonexecutableException() {
        // one of my colleagues has had an executable /etc/hosts (probably chmod'd everything 777 to try to get stuff working)
        // so now instead will make this a bit more resilient for such things
        String path = "/etc/hosts";
        File f = new File(path);
        if(!f.canExecute()) {
            validateProgramPath(path, path);
        } else {
            // your /etc/hosts has been set as executable, so bypassing test in locally flawed environment
            throw new IllegalArgumentException();
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateProgramPathInvalidregexException() {
        validateProgramPath("/bin/sh", "sh", "(.*");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateProgramPathNomatchregexException() {
        validateProgramPath("/bin/sh", "sh", ".*/bash$");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateProgramPathNullnameException() {
        validateProgramPath("/bin/sh", null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateProgramPathNullException() {
        validateProgramPath(null, "null");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateProgramPathBlankException() {
        validateProgramPath(" ", "blank");
    }

    // ====================================================================== //
    @Test
    public void testIsRegex(){
        assertTrue(isRegex(".*"));
        assertTrue(isRegex("(.*)"));
        assertFalse(isRegex("(.*"));
        assertFalse(isRegex(null));
    }

    @Test
    public void testValidateRegex(){
        assertEquals("validateRegex(some[Rr]egex.*(capture))", "some[Rr]egex.*(capture)",  validateRegex("some[Rr]egex.*(capture)", "myRegex"));
        assertEquals("validateRegex(some[Rr]egex.*(capture), null, true)", "some[Rr]egex.*(capture)",  validateRegex("some[Rr]egex.*(capture)", null, true));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateRegexException() {
        validateRegex("(.*", "myString");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateRegexPosixshellescapesException() {
        validateRegex("$(badcommand)", "myString", true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateRegexPosixshellescapes2Exception() {
        validateRegex("`badcommand`", "myString", true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateRegexNullException() {
        validateRegex(null, "myString");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateRegexBlankException() {
        validateRegex(" ", "myString");
    }

    // ====================================================================== //
    @Test
    public void testIsUrl(){
        assertTrue(isUrl("www.google.com"));
        assertTrue(isUrl("http://www.google.com"));
        assertTrue(isUrl("https://gmail.com"));
        assertTrue(isUrl("1"));
        assertTrue(isUrl("http://cdh43:50070/dfsnodelist.jsp?whatNodes=LIVE"));
        assertTrue(isUrl("http://namenode:50070/dfshealth.html#tab-overview"));
        assertFalse(isUrl("-help"));
        assertFalse(isUrl(null));
    }

    @Test
    public void testValidateUrl(){
        assertEquals("validateUrl(www.google.com)",        "http://www.google.com", validateUrl(" www.google.com ", "name"));
        assertEquals("validateUrl(http://www.google.com)", "http://www.google.com", validateUrl(" http://www.google.com "));
        assertEquals("validateUrl(http://gmail.com)",      "http://gmail.com",      validateUrl(" http://gmail.com "));
        assertEquals("validateUrl(http://cdh43:50070/dfsnodelist.jsp?whatNodes=LIVE)",      "http://cdh43:50070/dfsnodelist.jsp?whatNodes=LIVE",      validateUrl(" http://cdh43:50070/dfsnodelist.jsp?whatNodes=LIVE "));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateUrlException() {
        validateUrl("-help");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateUrlNullException() {
        validateUrl(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateUrlBlankException() {
        validateUrl(" ");
    }

    // ====================================================================== //
    @Test
    public void testIsUrlPathSuffix(){
        assertTrue(isUrlPathSuffix("/"));
        assertTrue(isUrlPathSuffix("/?var=something"));
        assertTrue(isUrlPathSuffix("/dir1/file.php?var=something+else&var2=more%20stuff"));
        assertTrue(isUrlPathSuffix("/*"));
        assertTrue(isUrlPathSuffix("/~hari"));
        assertFalse(isUrlPathSuffix("hari"));
        assertFalse(isUrlPathSuffix(null));
    }

    @Test
    public void testValidateUrlPathSuffix(){
        assertEquals("validateUrlPathSuffix(/)", "/", validateUrlPathSuffix("/", "name"));
        assertEquals("validateUrlPathSuffix(/?var=something)", "/?var=something", validateUrlPathSuffix("/?var=something"));
        assertEquals("validateUrlPathSuffix(/dir1/file.php?var=something+else&var2=more%20stuff)", "/dir1/file.php?var=something+else&var2=more%20stuff", validateUrlPathSuffix("/dir1/file.php?var=something+else&var2=more%20stuff"));
        assertEquals("validateUrlPathSuffix(/*)", "/*", validateUrlPathSuffix("/*"));
        assertEquals("validateUrlPathSuffix(/~hari)", "/~hari", validateUrlPathSuffix("/~hari"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateUrlPathSuffixException() {
        validateUrlPathSuffix("hari");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateUrlPathSuffixBlankException() {
        validateUrlPathSuffix(" ");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateUrlPathSuffixNullException() {
        validateUrlPathSuffix(null);
    }

    // ====================================================================== //
    @Test
    public void testIsUser(){
        assertTrue(isUser("hadoop"));
        assertTrue(isUser("hari1"));
        assertTrue(isUser("mysql_test"));
        assertTrue(isUser("cloudera-scm"));
        assertTrue(isUser("hari.sekhon"));
        assertTrue(isUser("nonexistentuser"));
        assertFalse(isUser("-hari"));
        assertTrue(isUser("1hari"));
        assertTrue(isUser("12345678"));
        assertTrue(isUser("null"));
        assertFalse(isUser(null));
    }

    @Test
    public void testValidateUser(){
        assertEquals("validateUser(hadoop, name)", "hadoop", validateUser("hadoop", "name"));
        assertEquals("validateUser(hari1)", "hari1", validateUser("hari1"));
        assertEquals("validateUser(9hari1)", "9hari", validateUser("9hari"));
        assertEquals("validateUser(12345678)", "12345678", validateUser("12345678"));
        assertEquals("validateUser(hari.sekhon)", "hari.sekhon", validateUser("hari.sekhon"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateUserException() {
        validateUser("-hari");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateUserNullException() {
        validateUser(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateUserBlankException2() {
        validateUser(" ");
    }

    // ====================================================================== //

    @Test
    public void testValidateUserExists() throws IOException {
        if(isLinuxOrMac()){
            assertEquals("validateUserExists(root)", "root", validateUserExists("root", "root"));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateUserExistsException() throws IOException {
        if(isLinuxOrMac()) {
            validateUserExists("nonexistentuser", "nonexistentuser");
        }
    }

    // ====================================================================== //
    @Test
    public void testIsVersion(){
        assertTrue(isVersion("1"));
        assertTrue(isVersion("2.1.2"));
        assertTrue(isVersion("2.2.0.4"));
        assertTrue(isVersion("3.0"));
        assertFalse(isVersion("a"));
        assertFalse(isVersion("3a"));
        assertFalse(isVersion("1.0-2"));
        assertFalse(isVersion("1.0-a"));
        assertFalse(isVersion(null));
    }

    @Test
    public void testIsVersionLax(){
        assertTrue(isVersionLax("1"));
        assertTrue(isVersionLax("2.1.2"));
            assertTrue(isVersionLax("2.2.0.4"));
        assertTrue(isVersionLax("3.0"));
        assertFalse(isVersionLax("a"));
        assertTrue(isVersionLax("3a"));
        assertTrue(isVersionLax("1.0-2"));
        assertTrue(isVersionLax("1.0-a"));
        assertFalse(isVersionLax("hari"));
        assertFalse(isVersionLax(null));
    }

    // ====================================================================== //

    @Test
    public void testValidateDatabaseQuerySelectShow(){
        assertEquals("validateDatabaseQuerySelectShow(SELECT count(*) from database.table)", "SELECT count(*) from database.table;", validateDatabaseQuerySelectShow("SELECT count(*) from database.table;"));
        assertEquals("validateDatabaseQuerySelectShow(select count(*) from database.created_date)", "select count(*) from database.created_date", validateDatabaseQuerySelectShow("select count(*) from database.created_date"));
        assertEquals("validateDatabaseQuerySelectShow(SELECT count(*) from product_updates)", "SELECT count(*) from product_updates", validateDatabaseQuerySelectShow("SELECT count(*) from product_updates"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseQuerySelectShowDeleteException() {
        validateDatabaseQuerySelectShow("DELETE FROM myTable;");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseQuerySelectShowDropException() {
        validateDatabaseQuerySelectShow("select * from (DROP myTable);");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseQuerySelectShowNullException() {
        validateDatabaseQuerySelectShow(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateDatabaseQuerySelectShowBlankException() {
        validateDatabaseQuerySelectShow(" ");
    }

    // ====================================================================== //

    @Test
    public void testValidatePassword(){
        assertEquals("validatePassword(wh@tev3r)", "wh@tev3r", validatePassword("wh@tev3r"));
        assertEquals("validatePassword(wh@tev3r)", "wh@tev3r", validatePassword("wh@tev3r", "name"));
        assertEquals("validatePassword(wh@tev3r)", "$(badcommand)", validatePassword("$(badcommand)", "name", true));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidatePasswordException() {
        validatePassword("`badcommand`");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidatePasswordException2() {
        validatePassword("$(badcommand)");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidatePasswordNullException() {
        validatePassword(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidatePasswordBlankException() {
        validatePassword(" ");
    }

    // ====================================================================== //

    @Test
    public void testGetCallingMethod(){
        System.out.println("XXX: " + getCallingMethod());
        // ~ Java 6 - 7
        //assertEquals("getCallingMethod()", "sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)", getCallingMethod());
        // ~ Java 8 - 11
        //assertEquals("getCallingMethod()", "java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)", getCallingMethod());
        //assertTrue(getCallingMethod().contains(".reflect.NativeMethodAccessorImpl.invoke0(Native Method)"));
        //assertTrue(getCallingMethod().contains(".reflect.NativeMethodAccessorImpl.invoke0(Native Method)"));
        assertThat(getCallingMethod(), containsString(".reflect.NativeMethodAccessorImpl.invoke0(Native Method)"));
    }

    // ====================================================================== //

    @Test
    public void testValidateVlog(){
        vlog("vlog");
        vlog("vlog2");
        vlog("vlog3");
        vlog(null);
        vlog(null);
        vlog(null);
        vlogOption("vlogOption", "myOpt");
        vlogOption("vlogOption", true);
        vlogOption("vlogOption", false);
        vlogOption(null, "myOpt");
        vlogOption(null, false);
    }

    // ====================================================================== //

    @Test
    public void testUserExists() throws IOException {
        assertTrue(userExists("root"));
        assertFalse(userExists("nonexistent"));
        assertFalse(userExists("b@d"));
        assertFalse(userExists(" "));
        assertFalse(userExists(null));
    }

//    @Test
//    public void testVerbose(){
//        setVerbose(2);
//        assertEquals("getVerbose() 2", 2, getVerbose());
//        setVerbose(1);
//        assertEquals("getVerbose() 1", 1, getVerbose());
//        setVerbose(3);
//        assertEquals("getVerbose() 3", 3, getVerbose());
//        setVerbose(4);
//        setVerbose(5);
//        setVerbose(-1);
//        setVerbose(0);
//        setVerbose(2);
//    }


    @Test
    public void testVersion(){
        version();
    }

    @Test
    public void testGetVersion(){
        getVersion();
    }

    // ====================================================================== //
    @Test
    public void testValidateWhich() throws IOException {
        if(isLinuxOrMac()){
            assertThat("which(sh)", which("sh"), anyOf(is("/bin/sh"),   is("/usr/bin/sh")));
            assertThat("which(/bin/bash)", which("/bin/bash"), anyOf(is("/bin/bash"), is("/usr/bin/bash")));
        }
    }

    @Test(expected=IOException.class)
    public void testWhichNonexecutableException() throws IOException {
//        if(isLinuxOrMac()) {
            which("/etc/resolv.conf");
//        }
    }

    @Test(expected=IOException.class)
    public void testWhichNonexistentException() throws IOException {
        which("nonexistentprogram");
    }

    @Test(expected=IOException.class)
    public void testWhichFullpathNonexistentException() throws IOException {
        which("/explicit/nonexistent/path");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testWhichNullException() throws IOException {
        which(null);
    }

    // ====================================================================== //
    /*
    @Test
    public void test_sec2min(){
        assertEquals("sec2min(65)", "1:05", sec2min(65));
        assertEquals("sec2min(30)", "0:30",   sec2min(30));
        assertEquals("sec2min(3601)", "60:01", sec2min(3601));
        assertEquals("sec2min(0)", "0:00", sec2min(0));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_sec2min_exception_negative(){
        sec2min("-1");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_sec2min_exception_nonnumeric(){
        sec2min("aa");
    }
     */

    // ====================================================================== //

    // @Test(expected= IndexOutOfBoundsException.class)
    // public void test_validate_* {
    // change validate_* to throwing exceptions for better testing and wrap in a generic exception catcher in Nagios code to convert to one liners

    //@Test
    // public void test_validate* {
    //      try {
    //          ...
    //          fail("Expected a BlahException to be thrown");
    //      } catch (BlahException e) {
    //          assertThat(e.getMessage(), is("some message"));
    //      }
    // }

    //@Ignore("Test disabled, re-enable later")
    // or use this more flexible one which allows use of matchers such as .containsString()
    //@Rule
    //public ExpectedException thrown = ExpectedException.none();

    //@Ignore("Test is ignored as a demonstration")
    //@Test
    //public void validate_*() throw IndexOutOfBoundsException {
    //  thrown.expect(IndexOutOfBoundsException.class);
    //  thrown.expectMessage("exact msg");
    //  thrown.expectMessage(JUnitMatchers.containsString("partial msg"));
    //  // call code to generate exception
    //}
}
