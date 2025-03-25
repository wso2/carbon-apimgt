/*
 *   Copyright (c) {2025}, WSO2 LLC. (https://www.wso2.com)
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.impl.definitions;

import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OAS30ProcessorTest {

    private OpenAPI30SchemaProcessor processor;
    private static final String example = "example";
    private static final String number = "number";
    private static final String dateFormat = "date";


    @Before
    public void setUp() {
        processor = new OpenAPI30SchemaProcessor();  // Create the real object of OAS31To30Processor
    }

    @Test
    public void convertSchemaTest() {
        Schema<Object> schema = new Schema<>();
        schema.setExample(example);
        schema.setType(APIConstants.OPENAPI_STRING_DATA_TYPE);
        schema.setNullable(true);
        processor.convertSchema(schema);
        Assert.assertEquals(example, schema.getExample());
        Assert.assertNull(schema.getExamples());
        Assert.assertNull(schema.getTypes());
        Assert.assertEquals(APIConstants.OPENAPI_STRING_DATA_TYPE, schema.getType());
    }

    @Test
    public void extractReferenceFromArraySchemaTest() {
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> arraySchema = getArraySchemaWithRefMap();
        processor.extractReferenceFromSchema(arraySchema, context);
        assertArraySchema(arraySchema, context);
    }

    private void assertArraySchema(Schema<Object> arraySchema, OASParserUtil.SwaggerUpdateContext context) {
        Assert.assertEquals(APIConstants.OPENAPI_ARRAY_DATA_TYPE, arraySchema.getType());
        Assert.assertEquals(APIConstants.OPENAPI_OBJECT_DATA_TYPE, arraySchema.getItems().getType());
        Assert.assertEquals(APIConstants.OPENAPI_STRING_DATA_TYPE, arraySchema.getItems().getProperties()
                .get(OASSchemaProcessorConstants.number).getType());
        Assert.assertEquals(APIConstants.OPENAPI_STRING_DATA_TYPE, arraySchema.getItems().getProperties()
                .get(OASSchemaProcessorConstants.expiryDate).getType());
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.schemas).contains(
                OASParserUtil.getRefKey(OASSchemaProcessorConstants.ref1)));
    }


    private Schema<Object> getArraySchemaWithRefMap() {
        Schema<Object> arraySchema = new ArraySchema();
        arraySchema.setSpecVersion(SpecVersion.V30);

        arraySchema.setType(APIConstants.OPENAPI_ARRAY_DATA_TYPE);
        Schema<Object> objectSchema = new JsonSchema();
        objectSchema.setSpecVersion(SpecVersion.V30);
        objectSchema.setType(APIConstants.OPENAPI_OBJECT_DATA_TYPE);

        Map<String, Schema> properties = new HashMap<>();
        Schema<String> numberSchema = new StringSchema();
        numberSchema.setSpecVersion(SpecVersion.V30);
        numberSchema.setType(APIConstants.OPENAPI_STRING_DATA_TYPE);
        properties.put(number, numberSchema);

        Schema<String> expiryDateSchema = new StringSchema();
        expiryDateSchema.setSpecVersion(SpecVersion.V30);
        expiryDateSchema.setType(APIConstants.OPENAPI_STRING_DATA_TYPE);
        expiryDateSchema.setFormat(dateFormat);
        properties.put(OASSchemaProcessorConstants.expiryDate, expiryDateSchema);

        Schema<Object> cvvSchema = new Schema<>();
        cvvSchema.setSpecVersion(SpecVersion.V30);
        cvvSchema.set$ref(OASSchemaProcessorConstants.ref1);
        properties.put(OASSchemaProcessorConstants.cvv, cvvSchema);

        objectSchema.setProperties(properties);
        arraySchema.setItems(objectSchema);
        return arraySchema;
    }

    @Test
    public void extractReferenceFromMapSchemaTest() {
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> mapSchema = getMapSchemaWithAdditionalProperties();
        processor.extractReferenceFromSchema(mapSchema, context);
        Assert.assertEquals(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.schemas).size(), 1);
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.schemas)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.ref1)));
    }

    private Schema<Object> getMapSchemaWithAdditionalProperties() {
        Schema<Object> mapSchema = new MapSchema();
        mapSchema.setSpecVersion(SpecVersion.V30);
        mapSchema.setType(APIConstants.OPENAPI_OBJECT_DATA_TYPE);

        Schema<String> additionalPropertySchema = new StringSchema();
        additionalPropertySchema.setSpecVersion(SpecVersion.V30);
        additionalPropertySchema.setType(APIConstants.OPENAPI_STRING_DATA_TYPE);
        mapSchema.setAdditionalProperties(additionalPropertySchema);

        Schema<Object> refSchema = new Schema<>();
        refSchema.setSpecVersion(SpecVersion.V30);
        refSchema.set$ref(OASSchemaProcessorConstants.ref1);

        Schema<String> descriptionSchema = new StringSchema();
        descriptionSchema.setSpecVersion(SpecVersion.V30);
        descriptionSchema.setType(APIConstants.OPENAPI_STRING_DATA_TYPE);
        descriptionSchema.setNullable(true);

        Map<String, Schema> properties = new HashMap<>();
        properties.put(OASSchemaProcessorConstants.cardDetails, refSchema);
        properties.put(OASSchemaProcessorConstants.description, descriptionSchema);
        mapSchema.setProperties(properties);

        return mapSchema;
    }

    @Test
    public void extractReferenceFromAllOfSchemaTest() {
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> allOfSchema = getAllOfSchemaWithRef();
        processor.extractReferenceFromSchema(allOfSchema, context);
        Assert.assertEquals(2, context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.schemas).size());
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.schemas)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.ref2)));
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.schemas)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.ref3)));
    }

    @Test
    public void extractReferenceFromOneOfSchemaTest() {
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> oneOfSchemaWithRef = getOneOfSchemaWithRef();
        processor.extractReferenceFromSchema(oneOfSchemaWithRef, context);
        Assert.assertEquals(1, context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.schemas).size());
        Assert.assertTrue(context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.schemas)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.ref2)));
    }

    @Test
    public void extractReferenceFromOneOfSchemaWithRefArray() {
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> oneOfSchema = getOneOfSchemaWithRefArray();
        processor.extractReferenceFromSchema(oneOfSchema, context);
        Assert.assertEquals(2, context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.schemas).size());
    }

    @Test
    public void extractReferenceFromComposedArrayWithRef(){
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> oneOfSchema = getArraySchemaWithComposedRefs();
        processor.extractReferenceFromSchema(oneOfSchema, context);
        Assert.assertEquals(3, context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.schemas).size());
    }

    private Schema<Object> getOneOfSchemaWithRef() {
        Schema<Object> cardTypeSchema = new ComposedSchema();
        cardTypeSchema.setSpecVersion(SpecVersion.V30);

        Schema<String> visaMastercardSchema = new StringSchema();
        visaMastercardSchema.setSpecVersion(SpecVersion.V30);
        visaMastercardSchema.setType(APIConstants.OPENAPI_STRING_DATA_TYPE);
        visaMastercardSchema.setEnum(Arrays.asList(OASSchemaProcessorConstants.exampleCardType1,
                OASSchemaProcessorConstants.exampleCardType3));

        Schema<String> amexSchema = new StringSchema();
        amexSchema.setSpecVersion(SpecVersion.V30);
        amexSchema.setType(APIConstants.OPENAPI_STRING_DATA_TYPE);
        amexSchema.setEnum(Collections.singletonList(OASSchemaProcessorConstants.exampleCardType2));

        Schema<Object> baseResponseSchema = new Schema<>();
        baseResponseSchema.set$ref(OASSchemaProcessorConstants.ref2);

        List<Schema> oneOfList = new ArrayList<>();
        oneOfList.add(visaMastercardSchema);
        oneOfList.add(amexSchema);
        oneOfList.add(baseResponseSchema);
        cardTypeSchema.setOneOf(oneOfList);
        return cardTypeSchema;
    }

    private Schema<Object> getOneOfSchemaWithRefArray() {
        Schema<Object> cardSchema = new ComposedSchema();
        cardSchema.setSpecVersion(SpecVersion.V30);
        Schema<Object> ref1 = new Schema<>();
        ref1.set$ref(OASSchemaProcessorConstants.ref1);
        Schema<Object> ref2 = new Schema<>();
        ref2.set$ref(OASSchemaProcessorConstants.ref2);
        List<Schema> oneOfList = new ArrayList<>();
        oneOfList.add(ref1);
        oneOfList.add(ref2);
        cardSchema.setOneOf(oneOfList);
        return cardSchema;
    }

    private Schema<Object> getArraySchemaWithComposedRefs() {
        Schema<Object> arrayschema = new ArraySchema();
        arrayschema.setSpecVersion(SpecVersion.V30);
        arrayschema.setType(APIConstants.OPENAPI_ARRAY_DATA_TYPE);
        Schema<Object> composedItems = new ComposedSchema();

        List<Schema> anyOfList = new ArrayList<>();
        anyOfList.add(new Schema().$ref(OASSchemaProcessorConstants.ref1));
        List<Schema> oneOfList = new ArrayList<>();
        oneOfList.add(new Schema().$ref(OASSchemaProcessorConstants.ref2));
        oneOfList.add(new Schema().$ref(OASSchemaProcessorConstants.ref3));

        composedItems.setAnyOf(anyOfList);
        composedItems.setOneOf(oneOfList);
        return composedItems;
    }

    @Test
    public void extractReferenceFromAnyOfSchemaTest() {
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> anyOfSchemaWithRef = getAnyOfSchemaWithRef();
        processor.extractReferenceFromSchema(anyOfSchemaWithRef, context);
        assertAnyOfSchemaWithRef(context);
    }

    @Test
    public void extractReferenceFromStringSchemaTest() {
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<String> schema = getSimpleSchema();
        processor.extractReferenceFromSchema(schema, context);
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.schemas).isEmpty());
    }

    @Test
    public void extractReferenceFromComplexObjectSchemaTest() {
        //Object with ref, array with ref and allOf composed object (object with ref and direct ref prop).
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> schema = getComplexComposedObjectSchema();
        processor.extractReferenceFromSchema(schema, context);
        Map<String, Schema> properties = schema.getProperties();
        Schema<Object> composedObj = properties.get(OASSchemaProcessorConstants.composedObject);
        Assert.assertEquals(OASSchemaProcessorConstants.ref1, ((Schema<?>) composedObj.getAllOf().get(1)).get$ref());
        Assert.assertEquals(3, context.getReferenceObjectMapping().get(
                OASSchemaProcessorConstants.schemas).size());
        assertArraySchema(properties.get(OASSchemaProcessorConstants.arrayProperty), context);
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.schemas)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.ref2)));
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.schemas)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.ref3)));
    }

    @Test
    public void extractReferenceFromNestedComposedSchema() {
        //allOf inside anyOf
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> schema = getNestedComposedSchema();
        processor.extractReferenceFromSchema(schema, context);
        assertAnyOfSchemaWithRef(context);
    }

    @Test
    public void extractReferenceFromNestedArrayInsideOneOfSchema() {
        //oneOf with nested arrays
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> schema = getNestedArrayInsideOneOf();
        processor.extractReferenceFromSchema(schema, context);
        Assert.assertEquals(1, context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.schemas).size());
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.schemas)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.ref1)));
    }

    private Schema<Object> getNestedArrayInsideOneOf() {
        Schema<Object> oneOfSchema = new ComposedSchema();
        oneOfSchema.setSpecVersion(SpecVersion.V30);
        List<Schema> oneOfList = new ArrayList<>();
        Schema<Object> refSchema = new Schema<>();
        refSchema.set$ref(OASSchemaProcessorConstants.ref1);
        Schema<Object> arraySchema2 = new ArraySchema();
        arraySchema2.setType(APIConstants.OPENAPI_ARRAY_DATA_TYPE);
        arraySchema2.setItems(refSchema);

        Schema<Object> arraySchema1 = new ArraySchema();
        arraySchema1.setType(APIConstants.OPENAPI_ARRAY_DATA_TYPE);
        arraySchema1.setItems(arraySchema2);

        Schema<Object> arraySchema3 = new ArraySchema();
        arraySchema3.setType(APIConstants.OPENAPI_ARRAY_DATA_TYPE);
        Schema<String> intSchema = new StringSchema();
        intSchema.setType(APIConstants.OPENAPI_STRING_DATA_TYPE);
        arraySchema3.setItems(intSchema);

        oneOfList.add(arraySchema1);
        oneOfList.add(arraySchema3);
        oneOfSchema.setOneOf(oneOfList);
        return oneOfSchema;
    }

    private Schema<Object> getNestedComposedSchema() {
        Schema<Object> nestedSchema = new ComposedSchema();
        nestedSchema.setSpecVersion(SpecVersion.V30);

        Schema<String> firstOneOf = new StringSchema();
        firstOneOf.setSpecVersion(SpecVersion.V30);
        firstOneOf.setType(APIConstants.OPENAPI_STRING_DATA_TYPE);

        Schema<Object> secondOneOf = new ObjectSchema();
        secondOneOf.setSpecVersion(SpecVersion.V30);
        secondOneOf.setType(APIConstants.OPENAPI_OBJECT_DATA_TYPE);
        Map<String, Schema> secondOneOfProps = new HashMap<>();
        Schema<Object> anyOfSchema = getAnyOfSchemaWithRef();
        secondOneOfProps.put(OASSchemaProcessorConstants.anyOfProperty, anyOfSchema);
        secondOneOf.setProperties(secondOneOfProps);

        List<Schema> oneOfList = new ArrayList<>();
        oneOfList.add(firstOneOf);
        oneOfList.add(secondOneOf);
        nestedSchema.setOneOf(oneOfList);
        return nestedSchema;
    }

    private Schema<Object> getComplexComposedObjectSchema() {
        Schema<Object> complexSchema = new ObjectSchema();
        complexSchema.setSpecVersion(SpecVersion.V30);
        complexSchema.setType(APIConstants.OPENAPI_OBJECT_DATA_TYPE);

        Schema<Object> composedObjectSchema = new ComposedSchema();
        composedObjectSchema.setSpecVersion(SpecVersion.V30);
        composedObjectSchema.setType(APIConstants.OPENAPI_OBJECT_DATA_TYPE);

        Schema<Object> firstObject = new ObjectSchema();
        firstObject.setSpecVersion(SpecVersion.V30);
        firstObject.setType(APIConstants.OPENAPI_OBJECT_DATA_TYPE);
        firstObject.addProperty(OASSchemaProcessorConstants.cardDetails, new Schema()
                .$ref(OASSchemaProcessorConstants.ref3));
        firstObject.addProperty(OASSchemaProcessorConstants.cardId,
                new StringSchema().type(APIConstants.OPENAPI_STRING_DATA_TYPE));

        Schema<Object> secondObject = new Schema<>();
        secondObject.setSpecVersion(SpecVersion.V30);
        secondObject.set$ref(OASSchemaProcessorConstants.ref1);

        List<Schema> allOfList = new ArrayList<>();
        allOfList.add(firstObject);
        allOfList.add(secondObject);
        composedObjectSchema.setAllOf(allOfList);

        Schema<Object> arrayPropertySchema = getArraySchemaWithRefMap();

        Schema<Object> anotherObjectSchema = new Schema<>();
        anotherObjectSchema.setSpecVersion(SpecVersion.V30);
        anotherObjectSchema.set$ref(OASSchemaProcessorConstants.ref2);

        // Adding anotherObject to the main schema
        complexSchema.addProperty(OASSchemaProcessorConstants.composedObject, composedObjectSchema);
        complexSchema.addProperty(OASSchemaProcessorConstants.arrayProperty, arrayPropertySchema);
        complexSchema.addProperty(OASSchemaProcessorConstants.anotherObject, anotherObjectSchema);
        return complexSchema;
    }


    private Schema<String> getSimpleSchema() {
        Schema<String> stringTypeSchema = new StringSchema();
        stringTypeSchema.setSpecVersion(SpecVersion.V30);
        stringTypeSchema.setType(APIConstants.OPENAPI_STRING_DATA_TYPE);
        return stringTypeSchema;
    }

    private Schema<Object> getAnyOfSchemaWithRef() {
        Schema<Object> anyOfSchema = new ComposedSchema();
        anyOfSchema.setSpecVersion(SpecVersion.V30);

        Schema<Object> baseResponseSchema = new Schema<>();
        baseResponseSchema.set$ref(OASSchemaProcessorConstants.ref2);

        Schema<Object> objectSchema = new ObjectSchema();
        objectSchema.setSpecVersion(SpecVersion.V30);
        objectSchema.setType(APIConstants.OPENAPI_OBJECT_DATA_TYPE);

        Schema<String> cardIdSchema = new StringSchema();
        cardIdSchema.setSpecVersion(SpecVersion.V30);
        cardIdSchema.setType(APIConstants.OPENAPI_STRING_DATA_TYPE);

        Schema<Object> cardDetailsSchema = new Schema<>();
        cardDetailsSchema.setSpecVersion(SpecVersion.V30);
        cardDetailsSchema.set$ref(OASSchemaProcessorConstants.ref3);

        Map<String, Schema> properties = new HashMap<>();
        properties.put(OASSchemaProcessorConstants.cardId, cardIdSchema);
        properties.put(OASSchemaProcessorConstants.cardDetails, cardDetailsSchema);
        objectSchema.setProperties(properties);

        List<Schema> anyOfList = new ArrayList<>();
        anyOfList.add(baseResponseSchema);
        anyOfList.add(objectSchema);
        anyOfSchema.setAnyOf(anyOfList);
        return anyOfSchema;
    }

    private void assertAnyOfSchemaWithRef(OASParserUtil.SwaggerUpdateContext context) {
        Assert.assertEquals(2, context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.schemas).size());
        Assert.assertTrue(context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.schemas)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.ref2)));
        Assert.assertTrue(context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.schemas)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.ref3)));
    }

    private Schema<Object> getAllOfSchemaWithRef() {
        Schema<Object> allOfSchema = new ComposedSchema();
        allOfSchema.setSpecVersion(SpecVersion.V30);

        Schema<Object> baseResponseSchema = new Schema<>();
        baseResponseSchema.set$ref(OASSchemaProcessorConstants.ref2);

        Schema<Object> objectSchema = new ObjectSchema();
        objectSchema.setSpecVersion(SpecVersion.V30);
        objectSchema.setType(APIConstants.OPENAPI_OBJECT_DATA_TYPE);

        Schema<String> cardIdSchema = new StringSchema();
        cardIdSchema.setSpecVersion(SpecVersion.V30);
        cardIdSchema.setType(APIConstants.OPENAPI_STRING_DATA_TYPE);

        Schema<Object> cardDetailsSchema = new Schema<>();
        cardDetailsSchema.setSpecVersion(SpecVersion.V30);
        cardDetailsSchema.set$ref(OASSchemaProcessorConstants.ref3);

        Map<String, Schema> properties = new HashMap<>();
        properties.put(OASSchemaProcessorConstants.cardId, cardIdSchema);
        properties.put(OASSchemaProcessorConstants.cardDetails, cardDetailsSchema);
        objectSchema.setProperties(properties);

        List<Schema> allOfList = new ArrayList<>();
        allOfList.add(baseResponseSchema);
        allOfList.add(objectSchema);
        allOfSchema.setAllOf(allOfList);
        return allOfSchema;
    }
}
