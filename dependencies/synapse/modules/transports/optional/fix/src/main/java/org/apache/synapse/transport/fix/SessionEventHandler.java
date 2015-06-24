/*
*  Licensed to the Apache Software Foundation (ASF) under one
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information
*  regarding copyright ownership.  The ASF licenses this file
*  to you under the Apache License, Version 2.0 (the
*  "License"); you may not use this file except in compliance
*  with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.apache.synapse.transport.fix;

public interface SessionEventHandler {

    void onCreate(quickfix.SessionID sessionID);

    void onLogon(quickfix.SessionID sessionID);

    void onLogout(quickfix.SessionID sessionID);

    void toAdmin(quickfix.Message message, quickfix.SessionID sessionID);

    void fromAdmin(quickfix.Message message, quickfix.SessionID sessionID);

    void toApp(quickfix.Message message, quickfix.SessionID sessionID);

}
