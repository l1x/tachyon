package tachyon.examples;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.log4j.Logger;

import tachyon.CommonUtils;
import tachyon.Version;
import tachyon.client.OpType;
import tachyon.client.TachyonClient;
import tachyon.client.TachyonFile;
import tachyon.conf.CommonConf;
import tachyon.thrift.FileAlreadyExistException;
import tachyon.thrift.InvalidPathException;
import tachyon.thrift.SuspectedFileSizeException;

public class BasicOperations {
  private static Logger LOG = Logger.getLogger(CommonConf.LOGGER_TYPE);

  private static TachyonClient sTachyonClient;
  private static String sFilePath = null;
  private static OpType sWriteType = null;

  public static void createFile() throws InvalidPathException, FileAlreadyExistException {
    long startTimeMs = CommonUtils.getCurrentMs();
    int fileId = sTachyonClient.createFile(sFilePath);
    CommonUtils.printTimeTakenMs(startTimeMs, LOG, "createFile with fileId " + fileId);
  }

  public static void writeFile()
      throws SuspectedFileSizeException, InvalidPathException, IOException {
    TachyonFile file = sTachyonClient.getFile(sFilePath);
    file.open(sWriteType);

    ByteBuffer buf = ByteBuffer.allocate(80);
    buf.order(ByteOrder.nativeOrder());
    for (int k = 0; k < 20; k ++) {
      buf.putInt(k);
    }

    buf.flip();
    LOG.info("Writing data...");
    CommonUtils.printByteBuffer(LOG, buf);
    buf.flip();
    file.append(buf);
    file.close();
  }

  public static void readFile()
      throws SuspectedFileSizeException, InvalidPathException, IOException {
    LOG.info("Reading data...");
    TachyonFile file = sTachyonClient.getFile(sFilePath);
    file.open(OpType.READ_TRY_CACHE);
    ByteBuffer buf = file.readByteBuffer();
    CommonUtils.printByteBuffer(LOG, buf);
    file.close();
  }

  public static void main(String[] args)
      throws SuspectedFileSizeException, InvalidPathException, IOException,
      FileAlreadyExistException {
    if (args.length != 3) {
      System.out.println("java -cp target/tachyon-" + Version.VERSION + 
          "-jar-with-dependencies.jar " +
          "tachyon.examples.BasicOperations <TachyonMasterAddress> <FilePath> <WriteType>");
      System.exit(-1);
    }
    sTachyonClient = TachyonClient.getClient(args[0]);
    sFilePath = args[1];
    sWriteType = OpType.getOpType(args[2]);
    createFile();
    writeFile();
    readFile();
  }
}