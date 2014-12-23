/*
 * Copyright 1999-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sk.seges.sesam.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.FileAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
* A FileAppender which creates a dated log file.  The rolling is performed by
* opening a new dated log file at midnight when the name changes.
* <p>
* It uses the time stamps of the incoming logging events to decide when it's time
* to perform the rollover, and tests for small clock drifts by checking that the
* file name actually changes.
* <p>
* It's used in the same way as FileAppender, except that the file name parameter
* implicitly appends .yyyy.MM.dd to the file name.
*
* @author Asgeir S. Nilsen
*/

public class DailyFileAppender extends FileAppender {
    
    static final long ONE_DAY = 86400000;
    
    protected SimpleDateFormat dateFormat = new SimpleDateFormat(".yyyy.MM.dd");
    protected String date = null;
    protected long midnight = 0;
    
    protected String baseFilename = null;
    
    public DailyFileAppender() {
        super();
        setupRoll();
    }
        
    /**
     * Performs rollOver() if the event timestamp is past midnight
     * 
     * @see org.apache.log4j.WriterAppender#subAppend(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
	protected void subAppend(LoggingEvent event) {
        if (event.timeStamp > midnight) 
            rollOver();
        super.subAppend(event);
    }
    
    /**
     * Initializes the file name's date part, and calculates the first rollOver occurence.
     */
    protected void setupRoll() {
        Date now = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 500);
        midnight = c.getTimeInMillis() + ONE_DAY;
        date = dateFormat.format(now);        
    }
    
    /**
     * Performs rollOver of file name.  Checks to see if the file name actually changes.
     */
    protected void rollOver() {
        String newDate = dateFormat.format(new Date());
        if (!newDate.equals(date)) {
            midnight += ONE_DAY;
            date = newDate;
            super.setFile(baseFilename + date);
            activateOptions();
        }
    }
    
    @Override
	public String getFile() {
        return super.getFile();
    }
    
    public String getFileBase() {
        return baseFilename;
    }
    
    @Override
	public void setFile(String file) {
        baseFilename = file;
        super.setFile(baseFilename + date);
    }
    
}

