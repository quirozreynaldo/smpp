package org.smpp.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import com.logica.smpp.Connection;
import com.logica.smpp.ServerPDUEvent;
import com.logica.smpp.ServerPDUEventListener;
import com.logica.smpp.Session;
import com.logica.smpp.SmppObject;
import com.logica.smpp.TCPIPConnection;
import com.logica.smpp.debug.Debug;
import com.logica.smpp.debug.Event;
import com.logica.smpp.debug.FileDebug;
import com.logica.smpp.debug.FileEvent;
import com.logica.smpp.pdu.Address;
import com.logica.smpp.pdu.AddressRange;
import com.logica.smpp.pdu.BindReceiver;
import com.logica.smpp.pdu.BindRequest;
import com.logica.smpp.pdu.BindResponse;
import com.logica.smpp.pdu.BindTransciever;
import com.logica.smpp.pdu.BindTransmitter;
import com.logica.smpp.pdu.CancelSM;
import com.logica.smpp.pdu.CancelSMResp;
import com.logica.smpp.pdu.DataSM;
import com.logica.smpp.pdu.DataSMResp;
import com.logica.smpp.pdu.DeliverSM;
import com.logica.smpp.pdu.DeliverSMResp;
import com.logica.smpp.pdu.DestinationAddress;
import com.logica.smpp.pdu.EnquireLink;
import com.logica.smpp.pdu.EnquireLinkResp;
import com.logica.smpp.pdu.PDU;
import com.logica.smpp.pdu.QuerySM;
import com.logica.smpp.pdu.QuerySMResp;
import com.logica.smpp.pdu.ReplaceSM;
import com.logica.smpp.pdu.ReplaceSMResp;
import com.logica.smpp.pdu.Response;
import com.logica.smpp.pdu.SubmitMultiSM;
import com.logica.smpp.pdu.SubmitMultiSMResp;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.SubmitSMResp;
import com.logica.smpp.pdu.UnbindResp;
import com.logica.smpp.pdu.WrongLengthOfStringException;
import com.logica.smpp.util.Queue;

public class SMPPTest {
  static final String copyright = "Copyright (c) 1996-2001 Logica Mobile Networks Limited\nThis product includes software developed by Logica by whom copyright\nand know-how are retained, all rights reserved.\n";
  
  static final String version = "SMPP Open Source test & demonstration application, version 1.1\n";
  
  static final String dbgDir = "./";
  
  static {
    System.out.println("Copyright (c) 1996-2001 Logica Mobile Networks Limited\nThis product includes software developed by Logica by whom copyright\nand know-how are retained, all rights reserved.\n");
    System.out.println("SMPP Open Source test & demonstration application, version 1.1\n");
  }
  
  static Debug debug = (Debug)new FileDebug("./", "test.dbg");
  
  static Event event = (Event)new FileEvent("./", "test.evt");
  
  static String propsFilePath = "smpptest.cfg";
  
  static BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
  
  static Session session = null;
  
  Properties properties = new Properties();
  
  boolean bound = false;
  
  private boolean keepRunning = true;
  
  String ipAddress = null;
  
  int port = 0;
  
  String systemId = null;
  
  String password = null;
  
  String bindOption = "t";
  
  boolean asynchronous = false;
  
  SMPPTestPDUEventListener pduListener = null;
  
  AddressRange addressRange = new AddressRange();
  
  private EnquireThread enquireThread = new EnquireThread();
  
  String systemType = "";
  
  String serviceType = "";
  
  Address sourceAddress = new Address();
  
  Address destAddress = new Address();
  
  String scheduleDeliveryTime = "";
  
  String validityPeriod = "";
  
  String shortMessage = "";
  
  int numberOfDestination = 1;
  
  String messageId = "";
  
  byte esmClass = 0;
  
  byte protocolId = 0;
  
  byte priorityFlag = 0;
  
  byte registeredDelivery = 1;
  
  byte replaceIfPresentFlag = 0;
  
  byte dataCoding = 0;
  
  byte smDefaultMsgId = 0;
  
  long receiveTimeout = -1L;
  
  public SMPPTest() throws IOException {
    loadProperties(propsFilePath);
  }
  
  public static void main(String[] args) {
    System.out.println("Initialising...");
    System.out.println("Charset used: " + (new InputStreamReader(System.in)).getEncoding());
    debug.activate();
    event.activate();
    SmppObject.setDebug(debug);
    SmppObject.setEvent(event);
    SMPPTest test = null;
    try {
      test = new SMPPTest();
    } catch (IOException e) {
      event.write(e, "");
      debug.write("exception initialising SMPPTest " + e);
      System.out.println("Exception initialising SMPPTest " + e);
    } 
    if (test != null)
      test.menu(); 
  }
  
  public void menu() {
    this.keepRunning = true;
    String option = "1";
    while (this.keepRunning) {
      System.out.println();
      System.out.println("-  1 bind");
      System.out.println("-  2 submit (t/tr)");
      System.out.println("-  3 submit multi (t/tr)");
      System.out.println("-  4 data (t/tr)");
      System.out.println("-  5 query (t/tr)");
      System.out.println("-  6 replace (t/tr)");
      System.out.println("-  7 cancel (t/tr)");
      System.out.println("-  8 enquire link (t/tr)");
      System.out.println("-  9 unbind");
      System.out.println("- 10 receive message (tr/r)");
      System.out.println("-  0 exit");
      System.out.print("> ");
      int optionInt = -1;
      try {
        option = keyboard.readLine();
        optionInt = Integer.parseInt(option);
      } catch (Exception e) {
        debug.write("exception reading keyboard " + e);
        optionInt = -1;
      } 
      switch (optionInt) {
        case 1:
          bind();
          continue;
        case 2:
          submit();
          continue;
        case 3:
          submitMulti();
          continue;
        case 4:
          data();
          continue;
        case 5:
          query();
          continue;
        case 6:
          replace();
          continue;
        case 7:
          cancel();
          continue;
        case 8:
          enquireLink();
          continue;
        case 9:
          unbind();
          continue;
        case 10:
          receive();
          continue;
        case 0:
          exit();
          continue;
        case -1:
          continue;
      } 
      System.out.println("Invalid option. Choose between 0 and 10.");
    } 
  }
  
  private void bind() {
    debug.enter(this, "SMPPTest.bind()");
    BindTransciever bindTransciever=null;
    try {

      if (this.bound) {
        System.out.println("Already bound, unbind first.");
        return;
      } 
      BindRequest request = null;
      BindResponse response = null;
      String syncMode = this.asynchronous ? "a" : "s";
      syncMode = getParam("Asynchronous/Synchronnous Session? (a/s)", syncMode);
      if (syncMode.compareToIgnoreCase("a") == 0) {
        this.asynchronous = true;
      } else if (syncMode.compareToIgnoreCase("s") == 0) {
        this.asynchronous = false;
      } else {
        System.out.println("Invalid mode async/sync, expected a or s, got " + syncMode + ". Operation canceled.");
        return;
      } 
      this.bindOption = getParam("Transmitter/Receiver/Transciever (t/r/tr)", this.bindOption);
      if (this.bindOption.compareToIgnoreCase("t") == 0) {
        BindTransmitter bindTransmitter = new BindTransmitter();
      } else if (this.bindOption.compareToIgnoreCase("r") == 0) {
        BindReceiver bindReceiver = new BindReceiver();
      } else if (this.bindOption.compareToIgnoreCase("tr") == 0) {
        bindTransciever = new BindTransciever();
      } else {
        System.out.println("Invalid bind mode, expected t, r or tr, got " + this.bindOption + ". Operation canceled.");
        return;
      } 
      this.ipAddress = getParam("IP address of SMSC", this.ipAddress);
      this.port = getParam("Port number", this.port);
      TCPIPConnection connection = new TCPIPConnection(this.ipAddress, this.port);
      connection.setReceiveTimeout(20000L);
      session = new Session((Connection)connection);
      this.systemId = getParam("Your system ID", this.systemId);
      this.password = getParam("Your password", this.password);
      bindTransciever.setSystemId(this.systemId);
      bindTransciever.setPassword(this.password);
      this.systemType = getParam("System-Type", this.systemType);
      bindTransciever.setSystemType(this.systemType);
      System.out.println("Bind request " + bindTransciever.debugString());
      if (this.asynchronous) {
        this.pduListener = new SMPPTestPDUEventListener(session);
        response = session.bind((BindRequest)bindTransciever, this.pduListener);
      } else {
        response = session.bind((BindRequest)bindTransciever);
      } 
      System.out.println("Bind response " + response.debugString());
      if (response.getCommandStatus() == 0)
        this.bound = true; 
    } catch (Exception e) {
      event.write(e, "");
      debug.write("Bind operation failed. " + e);
      System.out.println("Bind operation failed. " + e);
    } finally {
      debug.exit(this);
    } 
    if (this.bound)
      this.enquireThread.start(); 
  }
  
  private void unbind() {
    debug.enter(this, "SMPPTest.unbind()");
    try {
      if (!this.bound) {
        System.out.println("Not bound, cannot unbind.");
        return;
      } 
      System.out.println("Going to unbind.");
      if (session.getReceiver().isReceiver())
        System.out.println("It can take a while to stop the receiver."); 
      UnbindResp response = session.unbind();
      System.out.println("Unbind response " + response.debugString());
      this.bound = false;
    } catch (Exception e) {
      event.write(e, "");
      debug.write("Unbind operation failed. " + e);
      System.out.println("Unbind operation failed. " + e);
    } finally {
      debug.exit(this);
    } 
  }
  
  private void submit() {
    debug.enter(this, "SMPPTest.submit()");
    try {
      SubmitSM request = new SubmitSM();
      this.serviceType = getParam("Service type", this.serviceType);
      this.sourceAddress = getAddress("Source", this.sourceAddress);
      this.destAddress = getAddress("Destination", this.destAddress);
      this.replaceIfPresentFlag = getParam("Replace if present flag", this.replaceIfPresentFlag);
      this.shortMessage = getParam("The short message", this.shortMessage);
      this.scheduleDeliveryTime = getParam("Schedule delivery time", this.scheduleDeliveryTime);
      this.validityPeriod = getParam("Validity period", this.validityPeriod);
      this.esmClass = getParam("Esm class", this.esmClass);
      this.protocolId = getParam("Protocol id", this.protocolId);
      this.priorityFlag = getParam("Priority flag", this.priorityFlag);
      this.registeredDelivery = getParam("Registered delivery", this.registeredDelivery);
      this.dataCoding = getParam("Data encoding", this.dataCoding);
      this.smDefaultMsgId = getParam("Sm default msg id", this.smDefaultMsgId);
      request.setServiceType(this.serviceType);
      request.setSourceAddr(this.sourceAddress);
      request.setDestAddr(this.destAddress);
      request.setReplaceIfPresentFlag(this.replaceIfPresentFlag);
      request.setShortMessage(this.shortMessage, "ASCII");
      request.setScheduleDeliveryTime(this.scheduleDeliveryTime);
      request.setValidityPeriod(this.validityPeriod);
      request.setEsmClass(this.esmClass);
      request.setProtocolId(this.protocolId);
      request.setPriorityFlag(this.priorityFlag);
      request.setRegisteredDelivery(this.registeredDelivery);
      request.setDataCoding(this.dataCoding);
      request.setSmDefaultMsgId(this.smDefaultMsgId);
      request.setUserMessageReference((short)23);
      request.setSequenceNumber(2);
      int count = 1;
      System.out.println();
      count = getParam("How many times to submit this message (load test)", count);
      for (int i = 0; i < count; i++) {
        System.out.print("#" + i + "  ");
        System.out.println("Submit request " + request.debugString());
        System.out.println("Request Sequence Number: " + request.getSequenceNumber());
        if (this.asynchronous) {
          session.submit(request);
        } else {
          SubmitSMResp response = session.submit(request);
          System.out.println("Submit response " + response.debugString() + "messageId: " + response.getMessageId());
          this.messageId = response.getMessageId();
        } 
      } 
      try {
        System.out.println(request.getUserMessageReference());
      } catch (Exception e1) {
        System.out.println("can not get the param");
      } 
    } catch (Exception e) {
      event.write(e, "");
      debug.write("Submit operation failed. " + e);
      System.out.println("Submit operation failed. " + e);
    } finally {
      debug.exit(this);
    } 
  }
  
  private void submitMulti() {
    debug.enter(this, "SMPPTest.submitMulti()");
    try {
      SubmitMultiSM request = new SubmitMultiSM();
      this.serviceType = getParam("Service type", this.serviceType);
      this.sourceAddress = getAddress("Source", this.sourceAddress);
      this.numberOfDestination = getParam("Number of destinations", this.numberOfDestination);
      for (int i = 0; i < this.numberOfDestination; i++)
        request.addDestAddress(new DestinationAddress(getAddress("Destination", this.destAddress))); 
      this.replaceIfPresentFlag = getParam("Replace if present flag", this.replaceIfPresentFlag);
      this.shortMessage = getParam("The short message", this.shortMessage);
      this.scheduleDeliveryTime = getParam("Schdule delivery time", this.scheduleDeliveryTime);
      this.validityPeriod = getParam("Validity period", this.validityPeriod);
      this.esmClass = getParam("Esm class", this.esmClass);
      this.protocolId = getParam("Protocol id", this.protocolId);
      this.priorityFlag = getParam("Priority flag", this.priorityFlag);
      this.registeredDelivery = getParam("Registered delivery", this.registeredDelivery);
      this.dataCoding = getParam("Data encoding", this.dataCoding);
      this.smDefaultMsgId = getParam("Sm default msg id", this.smDefaultMsgId);
      request.setServiceType(this.serviceType);
      request.setSourceAddr(this.sourceAddress);
      request.setReplaceIfPresentFlag(this.replaceIfPresentFlag);
      request.setShortMessage(this.shortMessage);
      request.setScheduleDeliveryTime(this.scheduleDeliveryTime);
      request.setValidityPeriod(this.validityPeriod);
      request.setEsmClass(this.esmClass);
      request.setProtocolId(this.protocolId);
      request.setPriorityFlag(this.priorityFlag);
      request.setRegisteredDelivery(this.registeredDelivery);
      request.setDataCoding(this.dataCoding);
      request.setSmDefaultMsgId(this.smDefaultMsgId);
      System.out.println("Submit Multi request " + request.debugString());
      if (this.asynchronous) {
        session.submitMulti(request);
      } else {
        SubmitMultiSMResp response = session.submitMulti(request);
        System.out.println("Submit Multi response " + response.debugString());
        this.messageId = response.getMessageId();
      } 
    } catch (Exception e) {
      event.write(e, "");
      debug.write("Submit Multi operation failed. " + e);
      System.out.println("Submit Multi operation failed. " + e);
    } finally {
      debug.exit(this);
    } 
  }
  
  private void replace() {
    debug.enter(this, "SMPPTest.replace()");
    try {
      ReplaceSM request = new ReplaceSM();
      this.messageId = getParam("Message id", this.messageId);
      this.sourceAddress = getAddress("Source", this.sourceAddress);
      this.shortMessage = getParam("The short message", this.shortMessage);
      this.scheduleDeliveryTime = getParam("Schedule delivery time", this.scheduleDeliveryTime);
      this.validityPeriod = getParam("Validity period", this.validityPeriod);
      this.registeredDelivery = getParam("Registered delivery", this.registeredDelivery);
      this.smDefaultMsgId = getParam("Sm default msg id", this.smDefaultMsgId);
      request.setMessageId(this.messageId);
      request.setSourceAddr(this.sourceAddress);
      request.setShortMessage(this.shortMessage);
      request.setScheduleDeliveryTime(this.scheduleDeliveryTime);
      request.setValidityPeriod(this.validityPeriod);
      request.setRegisteredDelivery(this.registeredDelivery);
      request.setSmDefaultMsgId(this.smDefaultMsgId);
      System.out.println("Replace request " + request.debugString());
      if (this.asynchronous) {
        session.replace(request);
      } else {
        ReplaceSMResp response = session.replace(request);
        System.out.println("Replace response " + response.debugString());
      } 
    } catch (Exception e) {
      event.write(e, "");
      debug.write("Replace operation failed. " + e);
      System.out.println("Replace operation failed. " + e);
    } finally {
      debug.exit(this);
    } 
  }
  
  private void cancel() {
    debug.enter(this, "SMPPTest.cancel()");
    try {
      CancelSM request = new CancelSM();
      this.serviceType = getParam("Service type", this.serviceType);
      this.messageId = getParam("Message id", this.messageId);
      this.sourceAddress = getAddress("Source", this.sourceAddress);
      this.destAddress = getAddress("Destination", this.destAddress);
      request.setServiceType(this.serviceType);
      request.setMessageId(this.messageId);
      request.setSourceAddr(this.sourceAddress);
      request.setDestAddr(this.destAddress);
      System.out.println("Cancel request " + request.debugString());
      if (this.asynchronous) {
        session.cancel(request);
      } else {
        CancelSMResp response = session.cancel(request);
        System.out.println("Cancel response " + response.debugString());
      } 
    } catch (Exception e) {
      event.write(e, "");
      debug.write("Cancel operation failed. " + e);
      System.out.println("Cancel operation failed. " + e);
    } finally {
      debug.exit(this);
    } 
  }
  
  private void data() {
    debug.enter(this, "SMPPTest.data()");
    try {
      DataSM request = new DataSM();
      this.serviceType = getParam("Service type", this.serviceType);
      this.sourceAddress = getAddress("Source", this.sourceAddress, 65);
      this.destAddress = getAddress("Destination", this.destAddress, 65);
      this.esmClass = getParam("Esm class", this.esmClass);
      this.registeredDelivery = getParam("Registered delivery", this.registeredDelivery);
      this.dataCoding = getParam("Data encoding", this.dataCoding);
      request.setServiceType(this.serviceType);
      request.setSourceAddr(this.sourceAddress);
      request.setDestAddr(this.destAddress);
      request.setEsmClass(this.esmClass);
      request.setRegisteredDelivery(this.registeredDelivery);
      request.setDataCoding(this.dataCoding);
      System.out.println("Data request " + request.debugString());
      if (this.asynchronous) {
        session.data(request);
      } else {
        DataSMResp response = session.data(request);
        System.out.println("Data response " + response.debugString());
        this.messageId = response.getMessageId();
      } 
    } catch (Exception e) {
      event.write(e, "");
      debug.write("Data operation failed. " + e);
      System.out.println("Data operation failed. " + e);
    } finally {
      debug.exit(this);
    } 
  }
  
  private void query() {
    debug.enter(this, "SMPPTest.query()");
    try {
      QuerySM request = new QuerySM();
      this.messageId = getParam("Message id", this.messageId);
      this.sourceAddress = getAddress("Source", this.sourceAddress);
      request.setMessageId(this.messageId);
      request.setSourceAddr(this.sourceAddress);
      System.out.println("Query request " + request.debugString());
      if (this.asynchronous) {
        session.query(request);
      } else {
        QuerySMResp response = session.query(request);
        System.out.println("Query response " + response.debugString());
        this.messageId = response.getMessageId();
      } 
    } catch (Exception e) {
      event.write(e, "");
      debug.write("Query operation failed. " + e);
      System.out.println("Query operation failed. " + e);
    } finally {
      debug.exit(this);
    } 
  }
  
  private void enquireLink() {
    debug.enter(this, "SMPPTest.enquireLink()");
    try {
      EnquireLink request = new EnquireLink();
      System.out.println("Enquire Link request " + request.debugString());
      if (this.asynchronous) {
        session.enquireLink(request);
      } else {
        EnquireLinkResp response = session.enquireLink(request);
        System.out.println("Enquire Link response " + response.debugString());
      } 
    } catch (Exception e) {
      event.write(e, "");
      debug.write("Enquire Link operation failed. " + e);
      System.out.println("Enquire Link operation failed. " + e);
    } finally {
      debug.exit(this);
    } 
  }
  
  private void receive() {
    debug.enter(this, "SMPPTest.receive()");
    try {
      DeliverSM pdu = null;
      System.out.print("Going to receive a PDU. ");
      if (this.receiveTimeout == -1L) {
        System.out.print("The receive is blocking, i.e. the application will stop until a PDU will be received.");
      } else {
        System.out.print("The receive timeout is " + (this.receiveTimeout / 1000L) + " sec.");
      } 
      System.out.println();
      if (this.asynchronous) {
        ServerPDUEvent pduEvent = this.pduListener.getRequestEvent(this.receiveTimeout);
        if (pduEvent != null)
          pdu = (DeliverSM)pduEvent.getPDU(); 
      } else {
        pdu = (DeliverSM)session.receive(this.receiveTimeout);
      } 
      if (pdu != null) {
        System.out.println("Received PDU " + pdu.debugString());
        try {
          System.out.println(pdu.getUserMessageReference());
        } catch (Exception e1) {
          System.out.println("can not get the param");
        } 
        System.out.println(pdu.getDestAddr().getAddress());
        System.out.println(pdu.getSourceAddr().getAddress());
        if (pdu.isRequest()) {
          DeliverSMResp response = (DeliverSMResp)pdu.getResponse();
          System.out.println(pdu.getEsmClass());
          System.out.println(pdu.getReceiptedMessageId());
          System.out.println(pdu.getCommandStatus());
          System.out.println("Going to send default response to request " + response.debugString());
          session.respond((Response)response);
        } 
      } else {
        System.out.println("No PDU received this time.");
      } 
    } catch (Exception e) {
      event.write(e, "");
      debug.write("Receiving failed. " + e);
      System.out.println("Receiving failed. " + e);
    } finally {
      debug.exit(this);
    } 
  }
  
  private void benchmark_submit() {
    debug.enter(this, "SMPPTest.submit()");
    try {
      SubmitSM request = new SubmitSM();
      this.shortMessage = "testingMessage";
      this.registeredDelivery = 1;
      int tps = getParam("tps", 20);
      request.setServiceType(this.serviceType);
      request.setSourceAddr(this.sourceAddress);
      request.setDestAddr(this.destAddress);
      request.setReplaceIfPresentFlag(this.replaceIfPresentFlag);
      request.setShortMessage(this.shortMessage, "ASCII");
      request.setScheduleDeliveryTime(this.scheduleDeliveryTime);
      request.setValidityPeriod(this.validityPeriod);
      request.setEsmClass(this.esmClass);
      request.setProtocolId(this.protocolId);
      request.setPriorityFlag(this.priorityFlag);
      request.setRegisteredDelivery(this.registeredDelivery);
      request.setDataCoding(this.dataCoding);
      request.setSmDefaultMsgId(this.smDefaultMsgId);
      request.setUserMessageReference((short)23);
      request.setSequenceNumber(2);
      int count = 1;
      System.out.println();
      count = getParam("How many times to submit this message (load test)", count);
      for (int i = 0; i < count; i++) {
        System.out.print("#" + i + "  ");
        System.out.println("Submit request " + request.debugString());
        System.out.println("Request Sequence Number: " + request.getSequenceNumber());
        if (this.asynchronous) {
          session.submit(request);
        } else {
          SubmitSMResp response = session.submit(request);
          System.out.println("Submit response " + response.debugString() + "messageId: " + response.getMessageId());
          this.messageId = response.getMessageId();
        } 
      } 
      try {
        System.out.println(request.getUserMessageReference());
      } catch (Exception e1) {
        System.out.println("can not get the param");
      } 
    } catch (Exception e) {
      event.write(e, "");
      debug.write("Submit operation failed. " + e);
      System.out.println("Submit operation failed. " + e);
    } finally {
      debug.exit(this);
    } 
  }
  
  private void exit() {
    debug.enter(this, "SMPPTest.exit()");
    if (this.bound)
      unbind(); 
    this.keepRunning = false;
    debug.exit(this);
  }
  
  private class SMPPTestPDUEventListener extends SmppObject implements ServerPDUEventListener {
    Session session;
    
    Queue requestEvents = new Queue();
    
    public SMPPTestPDUEventListener(Session session) {
      this.session = session;
    }
    
    public void handleEvent(ServerPDUEvent event) {
      PDU pdu = event.getPDU();
      if (pdu.isRequest()) {
        System.out.println("async request received, enqueuing " + pdu.debugString());
        synchronized (this.requestEvents) {
          this.requestEvents.enqueue(event);
          this.requestEvents.notify();
        } 
      } else if (pdu.isResponse()) {
        System.out.println("async response received " + pdu.debugString());
      } else {
        System.out.println("pdu of unknown class (not request nor response) received, discarding " + pdu.debugString());
      } 
    }
    
    public ServerPDUEvent getRequestEvent(long timeout) {
      ServerPDUEvent pduEvent = null;
      synchronized (this.requestEvents) {
        if (this.requestEvents.isEmpty())
          try {
            this.requestEvents.wait(timeout);
          } catch (InterruptedException e) {} 
        if (!this.requestEvents.isEmpty())
          pduEvent = (ServerPDUEvent)this.requestEvents.dequeue(); 
      } 
      return pduEvent;
    }
  }
  
  private String getParam(String prompt, String defaultValue) {
    String value = "";
    String promptFull = prompt;
    promptFull = promptFull + ((defaultValue == null) ? "" : (" [" + defaultValue + "] "));
    System.out.print(promptFull);
    try {
      value = keyboard.readLine();
    } catch (IOException e) {
      event.write(e, "");
      debug.write("Got exception getting a param. " + e);
    } 
    if (value.compareTo("") == 0)
      return defaultValue; 
    return value;
  }
  
  private byte getParam(String prompt, byte defaultValue) {
    return Byte.parseByte(getParam(prompt, Byte.toString(defaultValue)));
  }
  
  private int getParam(String prompt, int defaultValue) {
    return Integer.parseInt(getParam(prompt, Integer.toString(defaultValue)));
  }
  
  private Address getAddress(String type, Address address, int maxAddressLength) throws WrongLengthOfStringException {
    byte ton = getParam(type + " address TON", address.getTon());
    byte npi = getParam(type + " address NPI", address.getNpi());
    String addr = getParam(type + " address", address.getAddress());
    address.setTon(ton);
    address.setNpi(npi);
    address.setAddress(addr, maxAddressLength);
    return address;
  }
  
  private Address getAddress(String type, Address address) throws WrongLengthOfStringException {
    return getAddress(type, address, 21);
  }
  
  private void loadProperties(String fileName) throws IOException {
    System.out.println("Reading configuration file " + fileName + "...");
    FileInputStream propsFile = new FileInputStream(fileName);
    this.properties.load(propsFile);
    propsFile.close();
    System.out.println("Setting default parameters...");
    this.ipAddress = this.properties.getProperty("ip-address");
    this.port = getIntProperty("port", this.port);
    this.systemId = this.properties.getProperty("system-id");
    this.password = this.properties.getProperty("password");
    byte ton = getByteProperty("addr-ton", this.addressRange.getTon());
    byte npi = getByteProperty("addr-npi", this.addressRange.getNpi());
    String addr = this.properties.getProperty("address-range", this.addressRange.getAddressRange());
    this.addressRange.setTon(ton);
    this.addressRange.setNpi(npi);
    try {
      this.addressRange.setAddressRange(addr);
    } catch (WrongLengthOfStringException e) {
      System.out.println("The length of address-range parameter is wrong.");
    } 
    ton = getByteProperty("source-ton", this.sourceAddress.getTon());
    npi = getByteProperty("source-npi", this.sourceAddress.getNpi());
    addr = this.properties.getProperty("source-address", this.sourceAddress.getAddress());
    setAddressParameter("source-address", this.sourceAddress, ton, npi, addr);
    ton = getByteProperty("destination-ton", this.destAddress.getTon());
    npi = getByteProperty("destination-npi", this.destAddress.getNpi());
    addr = this.properties.getProperty("destination-address", this.destAddress.getAddress());
    setAddressParameter("destination-address", this.destAddress, ton, npi, addr);
    this.serviceType = this.properties.getProperty("service-type", this.serviceType);
    this.systemType = this.properties.getProperty("system-type", this.systemType);
    String bindMode = this.properties.getProperty("bind-mode", this.bindOption);
    if (bindMode.equalsIgnoreCase("transmitter")) {
      bindMode = "t";
    } else if (bindMode.equalsIgnoreCase("receiver")) {
      bindMode = "r";
    } else if (bindMode.equalsIgnoreCase("transciever")) {
      bindMode = "tr";
    } else if (!bindMode.equalsIgnoreCase("t") && !bindMode.equalsIgnoreCase("r") && !bindMode.equalsIgnoreCase("tr")) {
      System.out.println("The value of bind-mode parameter in the configuration file " + fileName + " is wrong. " + "Setting the default");
      bindMode = "t";
    } 
    this.bindOption = bindMode;
    int rcvTimeout = 10; //getIntProperty("receive-timeout", rcvTimeout);
    if (this.receiveTimeout == -1L) {
      rcvTimeout = -1;
    } else {
      rcvTimeout = (int)this.receiveTimeout / 1000;
    } 
     rcvTimeout = getIntProperty("receive-timeout", rcvTimeout);
    if (rcvTimeout == -1) {
      this.receiveTimeout = -1L;
    } else {
      this.receiveTimeout = (rcvTimeout * 1000);
    } 
    String syncMode = this.properties.getProperty("sync-mode", this.asynchronous ? "async" : "sync");
    if (syncMode.equalsIgnoreCase("sync")) {
      this.asynchronous = false;
    } else if (syncMode.equalsIgnoreCase("async")) {
      this.asynchronous = true;
    } else {
      this.asynchronous = false;
    } 
  }
  
  private byte getByteProperty(String propName, byte defaultValue) {
    return Byte.parseByte(this.properties.getProperty(propName, Byte.toString(defaultValue)));
  }
  
  private int getIntProperty(String propName, int defaultValue) {
    return Integer.parseInt(this.properties.getProperty(propName, Integer.toString(defaultValue)));
  }
  
  private void setAddressParameter(String descr, Address address, byte ton, byte npi, String addr) {
    address.setTon(ton);
    address.setNpi(npi);
    try {
      address.setAddress(addr);
    } catch (WrongLengthOfStringException e) {
      System.out.println("The length of " + descr + " parameter is wrong.");
    } 
  }
  
  private class EnquireThread extends Thread {
    private EnquireThread() {}
    
    public void run() {
      while (true) {
        try {
          while (true) {
            SMPPTest.this.enquireLink();
            sleep(30000L);
          }
        } catch (Exception e) {
          System.out.println(e);
        } 
      } 
    }
  }
}
