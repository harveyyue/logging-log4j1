/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j;

import org.apache.log4j.helpers.LogLog;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <b>Important Note:</b>
 * This is modified version of <code>DailyRollingFileAppender</code>. I have just added <code>maxBackupIndex</code>. So, if your number of log files increased more than <code>maxBackupIndex</code> it will delete the older log files.
 * The modified code only tested on Windows Operating System. If it have any issue on any other platform please modified it accordingly.
 *
 * @ModifiedBy: Bikash Shaw
 */
public class CustomDailyRollingFileAppender extends DailyRollingFileAppender {

  /**
   * There is one backup file by default.
   */
  protected int maxBackupIndex = 1;

  /**
   * The default constructor does nothing.
   */
  public CustomDailyRollingFileAppender() {
    super();
  }

  /**
   * Instantiate a <code>DailyRollingFileAppender</code> and open the
   * file designated by <code>filename</code>. The opened filename will
   * become the ouput destination for this appender.
   */
  public CustomDailyRollingFileAppender(Layout layout, String filename,
                                        String datePattern) throws IOException {
    super(layout, filename, datePattern);
  }

  /**
   * Set the maximum number of backup files to keep around.
   *
   * <p>The <b>MaxBackupIndex</b> option determines how many backup
   * files are kept before the oldest is erased. This option takes
   * a positive integer value. If set to zero, then there will be no
   * backup files and the log file will be truncated when it reaches
   * <code>MaxFileSize</code>.
   */
  public void setMaxBackupIndex(int maxBackups) {
    this.maxBackupIndex = maxBackups;
  }

  /**
   * Returns the value of the <b>MaxBackupIndex</b> option.
   */
  public int getMaxBackupIndex() {
    return maxBackupIndex;
  }

  /**
   * Rollover the current file to a new file.
   */
  void rollOver() throws IOException {
    super.rollOver();

    LogLog.debug("maxBackupIndex: " + maxBackupIndex);
    List<ModifiedTimeSortableFile> files = getAllFiles();
    Collections.sort(files);
    if (files.size() > maxBackupIndex) {
      int index = 0;
      int diff = files.size() - (maxBackupIndex + 1);
      for (ModifiedTimeSortableFile file : files) {
        if (index >= diff) {
          break;
        }
        file.delete();
        index++;
      }
    }
  }

  /**
   * This method searches list of log files
   * based on the pattern given in the log4j configuration file
   * and returns a collection
   *
   * @return List&lt;ModifiedTimeSortableFile&gt;
   */
  private List<ModifiedTimeSortableFile> getAllFiles() {
    File file = new File(fileName);
    String parentDirectory = file.getParent();
    final String localFile = fileName.substring(parentDirectory.length() + 1);
    LogLog.debug("directory name: " + parentDirectory + ", current file name: " + localFile);
    if (file.exists()) {
      if (file.getParent() == null) {
        String absolutePath = file.getAbsolutePath();
        parentDirectory = absolutePath.substring(0, absolutePath.lastIndexOf(fileName));
      }
    }
    FilenameFilter filter = (dir, name) -> name.startsWith(localFile);
    File dir = new File(parentDirectory);
    String[] names = dir.list(filter);
    return Arrays.stream(names)
        .map(name -> new ModifiedTimeSortableFile(dir + System.getProperty("file.separator") + name))
        .collect(Collectors.toList());
  }
}

/**
 * The Class ModifiedTimeSortableFile extends java.io.File class and
 * implements Comparable to sort files list based upon their modified date
 */
class ModifiedTimeSortableFile extends File implements Serializable, Comparable<File> {
  private static final long serialVersionUID = 1373373728209668895L;

  public ModifiedTimeSortableFile(String parent, String child) {
    super(parent, child);
    // TODO Auto-generated constructor stub
  }

  public ModifiedTimeSortableFile(URI uri) {
    super(uri);
    // TODO Auto-generated constructor stub
  }

  public ModifiedTimeSortableFile(File parent, String child) {
    super(parent, child);
  }

  public ModifiedTimeSortableFile(String string) {
    super(string);
  }

  @Override
  public int compareTo(File anotherPathName) {
    long thisVal = this.lastModified();
    long anotherVal = anotherPathName.lastModified();
    return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
  }
}
