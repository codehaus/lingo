/** 
 * 
 * Copyright 2006 LogicBlaze, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.logicblaze.lingo.cache;

/**
 * Defines the different supported transaction isolation levels. Knowledge of
 * databases and transactions can help understand these different values.
 *
 * @version $Revision$
 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
 */
public interface TransactionIsolation {

    /**
     * A transaction isolation level where all reads only read data that was committed by
     * other asynchronous background transactions.
     * Re-reading data for the same key during a transaction
     * could change its value as during the transaction a background process could commit
     * a transaction and update the data.
     * <p/>
     * Dirty reads are prevented; non-repeatable reads and phantom  reads can occur.
     *
     * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
     */
    public static final int READ_COMMITTED = 1;

    /**
     * A transaction isolation level where any data read during a transaction will always
     * stay the same within a transaction, even if concurrent updates are taking place.
     * <p/>
     * Dirty reads and non-repeatable reads are prevented; phantom  reads can occur.
     *
     * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
     */
    public static final int REPEATABLE_READ = 2;

    /**
     * Dirty reads, non-repeatable reads and phantom reads are prevented.
     *
     * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
     */
    public static final int SERIALIZEABLE = 2;

}
