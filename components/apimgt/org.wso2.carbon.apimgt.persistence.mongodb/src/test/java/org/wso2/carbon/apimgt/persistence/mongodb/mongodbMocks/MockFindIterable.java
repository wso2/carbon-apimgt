/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.persistence.mongodb.mongodbMocks;

import com.mongodb.CursorType;
import com.mongodb.Function;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Collation;
import org.bson.conversions.Bson;

import java.util.Collection;
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MockFindIterable implements FindIterable {

    @Override
    public MongoCursor iterator() {
        return null;
    }

    @Override
    public void forEach(Consumer action) {

    }

    @Override
    public Spliterator spliterator() {
        return null;
    }

    @Override
    public MongoCursor cursor() {
        return null;
    }

    @Override
    public Object first() {
        MockAPIs mockAPIs = new MockAPIs();
        return mockAPIs.getSingleMongoDBAPI();
    }

    @Override
    public FindIterable filter(Bson bson) {
        return null;
    }

    @Override
    public FindIterable limit(int i) {
        return null;
    }

    @Override
    public FindIterable skip(int i) {
        return null;
    }

    @Override
    public FindIterable maxTime(long l, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public FindIterable maxAwaitTime(long l, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public FindIterable projection(Bson bson) {
        return new MockFindIterable();
    }

    @Override
    public FindIterable sort(Bson bson) {
        return null;
    }

    @Override
    public FindIterable noCursorTimeout(boolean b) {
        return null;
    }

    @Override
    public FindIterable oplogReplay(boolean b) {
        return null;
    }

    @Override
    public FindIterable partial(boolean b) {
        return null;
    }

    @Override
    public FindIterable cursorType(CursorType cursorType) {
        return null;
    }

    @Override
    public FindIterable batchSize(int i) {
        return null;
    }

    @Override
    public FindIterable collation(Collation collation) {
        return null;
    }

    @Override
    public FindIterable comment(String s) {
        return null;
    }

    @Override
    public FindIterable hint(Bson bson) {
        return null;
    }

    @Override
    public FindIterable hintString(String s) {
        return null;
    }

    @Override
    public FindIterable max(Bson bson) {
        return null;
    }

    @Override
    public FindIterable min(Bson bson) {
        return null;
    }

    @Override
    public FindIterable returnKey(boolean b) {
        return null;
    }

    @Override
    public FindIterable showRecordId(boolean b) {
        return null;
    }

    @Override
    public FindIterable allowDiskUse(Boolean aBoolean) {
        return null;
    }

    @Override
    public Collection into(Collection collection) {
        return null;
    }

    @Override
    public MongoIterable map(Function function) {
        return null;
    }
}
