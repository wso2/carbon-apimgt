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
import io.swagger.v3.oas.models.media.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OAS31To30ProcessorTest {

    private OpenAPI31To30SchemaProcessor processor;

    @Before
    public void setUp() {
        processor = new OpenAPI31To30SchemaProcessor();  // Create the real object of OAS31To30Processor
    }

    @Test
    public void convertSchemaShouldSetExampleWhenExamplesExist() {
        Schema<Object> schema = new Schema<>();
        schema.addExample("example1");
        schema.addExample("example2");
        processor.convertSchema(schema);
        Assert.assertEquals("example1", schema.getExample());
    }

    @Test
    public void convertSchemaShouldNotModifyExampleIfAlreadySet() {
        Schema<Object> schema = new Schema<>();
        schema.setExample("existingExample");
        processor.convertSchema(schema);
        Assert.assertEquals("existingExample", schema.getExample());
    }

    @Test
    public void convertSchemaShouldSetNullableWhenTypesContainNullable() {
        Schema<Object> schema = new Schema<>();
        Set<String> types = new HashSet<>();
        types.add("null");
        types.add("string");
        schema.setTypes(types);
        processor.convertSchema(schema);
        Assert.assertEquals("string", schema.getType());
        Assert.assertEquals(true, schema.getNullable());
        Assert.assertFalse("null should be removed from types", schema.getTypes().contains("null"));
    }

    @Test
    public void convertSchemaShouldSetTypeWhenSingleTypeExists() {
        Schema<Object> schema = new Schema<>();
        Set<String> types = new HashSet<>();
        types.add("string");
        schema.setTypes(types);
        processor.convertSchema(schema);
        Assert.assertEquals("string", schema.getType());
    }

    @Test
    public void convertSchemaShouldNotSetTypeWhenMultipleTypesExist() {
        Schema<Object> schema = new Schema<>();
        Set<String> types = new HashSet<>();
        types.add("string");
        types.add("integer");
        schema.setTypes(types);
        processor.convertSchema(schema);
        Assert.assertNull(schema.getType()); // Type should not be set if there are multiple types
    }

    @Test
    public void convertSchemaShouldOnlySetTypeIfTypeIsNull() {
        Schema<Object> schema = new Schema<>();
        schema.setType("string");
        Set<String> types = new HashSet<>();
        types.add("integer");
        schema.setTypes(types);
        processor.convertSchema(schema);
        Assert.assertEquals("string", schema.getType()); // Type should not be changed if already set
    }

    @Test
    public void convertSchemaShouldNotModifyExampleWhenExamplesIsEmpty() {
        Schema<Object> schema = new Schema<>();
        schema.setExamples(new ArrayList<>());
        processor.convertSchema(schema);
        Assert.assertNull(schema.getExample());
    }

    @Test
    public void convertSchemaShouldIgnoreWhenSchemaIsNull() {
        processor.convertSchema(null);
        // No exception should be thrown
    }

    @Test
    public void extractReferenceFromArraySchemaTest() {
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> arraySchema = getArraySchemaWithRefMap();
        processor.extractReferenceFromSchema(arraySchema, context);
        assertArraySchema(arraySchema, context);
    }

    @Test
    public void extractReferenceFromMapSchemaTest() {
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> mapSchema = getMapSchemaWithAdditionalProperties();
        processor.extractReferenceFromSchema(mapSchema, context);
        Assert.assertEquals(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE, mapSchema.getType());
        Schema<?> additionalPropertySchema = (Schema<?>) mapSchema.getAdditionalProperties();
        Assert.assertEquals(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE, additionalPropertySchema.getType());

        Map<String, String> exampleMap = (Map<String, String>) additionalPropertySchema.getExample();
        Assert.assertEquals(OASSchemaProcessorConstants.EXAMPLE_VERSION,
                exampleMap.get(OASSchemaProcessorConstants.VERSION));
        Assert.assertEquals(OASSchemaProcessorConstants.EXAMPLE_ENVIRONMENT,
                exampleMap.get(OASSchemaProcessorConstants.ENVIRONMENT));
        Schema<?> descriptionSchema = mapSchema.getProperties().get(OASSchemaProcessorConstants.DESCRIPTION);
        Assert.assertEquals(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE, descriptionSchema.getType());
        Assert.assertTrue(descriptionSchema.getNullable());
        Assert.assertEquals(OASSchemaProcessorConstants.SAMPLE_DESCRIPTION, descriptionSchema.getExample());
        Assert.assertEquals(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.SCHEMAS).size(), 1);
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.SCHEMAS)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.REF_1)));
    }

    @Test
    public void extractReferenceFromAllOfSchemaTest() {
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> allOfSchema = getAllOfSchemaWithRef();
        processor.extractReferenceFromSchema(allOfSchema, context);
        List<Schema> allOf = allOfSchema.getAllOf();
        Schema<?> cardIdSchema = ((Schema<?>) allOf.get(1).getProperties().get(OASSchemaProcessorConstants.CARD_ID));
        Assert.assertEquals(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE, cardIdSchema.getType());
        Assert.assertEquals(OASSchemaProcessorConstants.EXAMPLE_CARD_ID, cardIdSchema.getExample());
        Assert.assertEquals(2, context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.SCHEMAS).size());
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.SCHEMAS)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.REF_2)));
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.SCHEMAS)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.REF_3)));
    }

    @Test
    public void extractReferenceFromOneOfSchemaTest() {
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> oneOfSchemaWithRef = getOneOfSchemaWithRef();
        processor.extractReferenceFromSchema(oneOfSchemaWithRef, context);
        Assert.assertEquals("VISA", oneOfSchemaWithRef.getExample());
        List<Schema> oneOfList = oneOfSchemaWithRef.getOneOf();
        Schema visaMastercardSchema = oneOfList.get(0);
        Assert.assertEquals(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE, visaMastercardSchema.getType());
        Schema amexSchema = oneOfList.get(1);
        Assert.assertEquals(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE, amexSchema.getType());
        Assert.assertEquals(1, context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.SCHEMAS).size());
        Assert.assertTrue(context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.SCHEMAS)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.REF_2)));
    }

    @Test
    public void extractReferenceFromAnyOfSchemaTest() {
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> anyOfSchemaWithRef = getAnyOfSchemaWithRef();
        processor.extractReferenceFromSchema(anyOfSchemaWithRef, context);
        assertAnyOfSchemaWithRef(anyOfSchemaWithRef, context);
    }

    @Test
    public void extractReferenceFromStringSchemaTest() {
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> schema = getSimpleSchema();
        processor.extractReferenceFromSchema(schema, context);
        Assert.assertEquals(OASSchemaProcessorConstants.EXAMPLE_CARD_ID, schema.getExample());
        Assert.assertEquals(APISpecParserConstants.STRING, schema.getType());
        Assert.assertTrue(schema.getNullable());
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.SCHEMAS).isEmpty());
    }

    @Test
    public void extractReferenceFromComplexObjectSchemaTest() {
        //Object with ref, array with ref and allOf composed object (object with ref and direct ref prop).
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> schema = getComplexComposedObjectSchema();
        processor.extractReferenceFromSchema(schema, context);
        Assert.assertEquals(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE, schema.getType());
        Map<String, Schema> properties = schema.getProperties();
        Schema<Object> composedObj = properties.get(OASSchemaProcessorConstants.COMPOSED_OBJECT);
        Assert.assertEquals(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE, composedObj.getType());
        Schema allOfObj = composedObj.getAllOf().get(0);
        Assert.assertEquals(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE, allOfObj.getType());
        Assert.assertEquals(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE, ((Schema<?>) allOfObj.getProperties()
                .get(OASSchemaProcessorConstants.CARD_ID)).getType());
        Assert.assertEquals(OASSchemaProcessorConstants.EXAMPLE_CARD_ID, ((Schema<?>) allOfObj.getProperties()
                .get(OASSchemaProcessorConstants.CARD_ID)).getExample());
        Assert.assertEquals(OASSchemaProcessorConstants.REF_1, ((Schema<?>) composedObj.getAllOf().get(1)).get$ref());
        Assert.assertEquals(3, context.getReferenceObjectMapping().get(
                OASSchemaProcessorConstants.SCHEMAS).size());
        assertArraySchema(properties.get(OASSchemaProcessorConstants.ARRAY_PROPERTY), context);
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.SCHEMAS)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.REF_2)));
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.SCHEMAS)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.REF_3)));
    }

    @Test
    public void extractReferenceFromNestedComposedSchema() {
        //allOf inside anyOf
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> schema = getNestedComposedSchema();
        processor.extractReferenceFromSchema(schema, context);
        Assert.assertEquals(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE, schema.getOneOf().get(0).getType());
        Schema anyOfSchema = schema.getOneOf().get(1);
        Assert.assertEquals(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE, anyOfSchema.getType());
        assertAnyOfSchemaWithRef((Schema) anyOfSchema.getProperties().get(OASSchemaProcessorConstants.ANY_OF_PROPERTY),
                context);
    }

    @Test
    public void extractReferenceFromNestedArrayInsideOneOfSchema() {
        //oneOf with nested arrays
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> schema = getNestedArrayInsideOneOf();
        processor.extractReferenceFromSchema(schema, context);
        Schema firstArray = schema.getOneOf().get(0);
        Assert.assertEquals(APISpecParserConstants.OPENAPI_ARRAY_DATA_TYPE, firstArray.getType());
        Assert.assertEquals(APISpecParserConstants.OPENAPI_ARRAY_DATA_TYPE, firstArray.getItems().getType());
        Schema secondArray = schema.getOneOf().get(1);
        Assert.assertEquals(APISpecParserConstants.OPENAPI_ARRAY_DATA_TYPE, secondArray.getType());
        Assert.assertEquals(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE, secondArray.getItems().getType());
        Assert.assertEquals(1, context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.SCHEMAS).size());
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.SCHEMAS)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.REF_1)));
    }

    @Test
    public void extractNestedMapSchemaWithAdditionalProperties() {
        //additionalProperties inside additionalProperties
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> schema = getNestedMapSchemaWithAdditionalProperties();
        processor.extractReferenceFromSchema(schema, context);
        Assert.assertEquals(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE, schema.getType());
        Schema description = schema.getProperties().get(OASSchemaProcessorConstants.DESCRIPTION);
        Assert.assertEquals(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE, description.getType());
        Assert.assertTrue(description.getNullable());
        Assert.assertEquals(OASSchemaProcessorConstants.EXAMPLE_CARD_ID, description.getExample());
        Schema version = schema.getProperties().get(OASSchemaProcessorConstants.VERSION);
        Assert.assertEquals(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE, version.getType());
        Object addProp = version.getAdditionalProperties();
        Assert.assertNotNull(addProp);
        Schema addPropSchema = (Schema) addProp;
        Assert.assertEquals(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE, addPropSchema.getType());
        Schema nestedAddProp = (Schema) addPropSchema.getAdditionalProperties();
        Assert.assertNotNull(nestedAddProp.get$ref());
        Assert.assertEquals(2, context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.SCHEMAS).size());
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.SCHEMAS)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.REF_1)));
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.SCHEMAS)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.REF_2)));
    }

    @Test
    public void extractNestedArraySchemaWithRefs() {
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> schema = getNestedArraySchemaWithRef();
        processor.extractReferenceFromSchema(schema, context);
        Assert.assertEquals(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE, schema.getType());
        Assert.assertNotNull(schema.getProperties());
        Assert.assertNotNull(schema.getProperties().get(OASSchemaProcessorConstants.CARD_DETAILS));
        Schema cardDetails = schema.getProperties().get(OASSchemaProcessorConstants.CARD_DETAILS).getItems();
        Assert.assertEquals(APISpecParserConstants.OPENAPI_ARRAY_DATA_TYPE, schema.getProperties()
                .get(OASSchemaProcessorConstants.CARD_DETAILS).getType());
        Assert.assertEquals(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE, cardDetails.getType());
        Map<String, Schema> cardDetailsProp = cardDetails.getProperties();
        Schema cvv = cardDetailsProp.get(OASSchemaProcessorConstants.CVV);
        Assert.assertEquals(APISpecParserConstants.OPENAPI_ARRAY_DATA_TYPE, cvv.getType());
        Assert.assertNotNull(cvv.getItems());
        Assert.assertNotNull(cvv.getItems().get$ref());
        Assert.assertEquals(1, context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.SCHEMAS).size());
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.SCHEMAS)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.REF_2)));
    }

    @Test
    public void extractReferenceFromOneOfSchemaWithRefArray() {
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> oneOfSchema = getOneOfSchemaWithRefArray();
        processor.extractReferenceFromSchema(oneOfSchema, context);
        Assert.assertEquals(2, context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.SCHEMAS).size());
    }

    @Test
    public void extractReferenceFromComposedArrayWithRef(){
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> oneOfSchema = getArraySchemaWithComposedRefs();
        processor.extractReferenceFromSchema(oneOfSchema, context);
        Assert.assertEquals(3, context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.SCHEMAS).size());
    }

    private void assertArraySchema(Schema<Object> arraySchema, OASParserUtil.SwaggerUpdateContext context) {
        Assert.assertEquals(APISpecParserConstants.OPENAPI_ARRAY_DATA_TYPE, arraySchema.getType());
        Assert.assertEquals(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE, arraySchema.getItems().getType());
        Assert.assertEquals(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE, arraySchema.getItems().getProperties()
                .get(OASSchemaProcessorConstants.NUMBER).getType());
        Assert.assertEquals(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE, arraySchema.getItems().getProperties()
                .get(OASSchemaProcessorConstants.EXPIRY_DATE).getType());
        Assert.assertEquals(OASSchemaProcessorConstants.EXAMPLE_DATE, arraySchema.getItems().getProperties()
                .get(OASSchemaProcessorConstants.EXPIRY_DATE).getExample());
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.SCHEMAS).contains(
                OASParserUtil.getRefKey(OASSchemaProcessorConstants.REF_1)));
    }


    private void assertAnyOfSchemaWithRef(Schema anyOfSchema, OASParserUtil.SwaggerUpdateContext context) {
        List<Schema> anyOf = (List<Schema>) anyOfSchema.getAnyOf();
        Schema<?> cardIdSchema = ((Schema<?>) anyOf.get(1).getProperties()
                .get(OASSchemaProcessorConstants.CARD_ID));
        Assert.assertEquals(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE, cardIdSchema.getType());
        Assert.assertEquals(OASSchemaProcessorConstants.EXAMPLE_CARD_ID, cardIdSchema.getExample());
        Assert.assertEquals(2, context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.SCHEMAS).size());
        Assert.assertTrue(context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.SCHEMAS)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.REF_2)));
        Assert.assertTrue(context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.SCHEMAS)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.REF_3)));
    }

    private Schema<Object> getNestedMapSchemaWithAdditionalProperties() {
        Schema<Object> parentSchema = new JsonSchema();
        parentSchema.setTypes(getObjectType());

        Schema<Object> descriptionSchema = new JsonSchema();
        descriptionSchema.setSpecVersion(SpecVersion.V31);
        Set<String> stringTypes = getStringType();
        stringTypes.add(APISpecParserConstants.OPENAPIV31_SCHEMA_TYPE_NULLABLE);
        descriptionSchema.setTypes(stringTypes);
        descriptionSchema.setExamples(getExamples());

        Schema<Object> refSchema = new JsonSchema();
        refSchema.setSpecVersion(SpecVersion.V31);
        refSchema.set$ref(OASSchemaProcessorConstants.REF_1);

        Schema<Object> nestedSchema = new JsonSchema();
        nestedSchema.setSpecVersion(SpecVersion.V31);
        nestedSchema.setTypes(getObjectType());

        Schema<Object> additionalPropertySchema = new JsonSchema();
        additionalPropertySchema.setSpecVersion(SpecVersion.V31);
        additionalPropertySchema.setTypes(getObjectType());
        additionalPropertySchema.setAdditionalProperties(new JsonSchema().$ref(OASSchemaProcessorConstants.REF_2));
        nestedSchema.setAdditionalProperties(additionalPropertySchema);

        Map<String, Schema> properties = new HashMap<>();
        properties.put(OASSchemaProcessorConstants.CARD_DETAILS, refSchema);
        properties.put(OASSchemaProcessorConstants.DESCRIPTION, descriptionSchema);
        properties.put(OASSchemaProcessorConstants.VERSION, nestedSchema);
        parentSchema.setProperties(properties);
        return parentSchema;
    }

    private Schema<Object> getNestedArraySchemaWithRef() {
        Schema<Object> nestedArrayObjectSchema = new JsonSchema();
        nestedArrayObjectSchema.setSpecVersion(SpecVersion.V31);
        nestedArrayObjectSchema.setTypes(getObjectType());
        Map<String, Schema> nestedProperties = new HashMap<>();

        Schema<Object> arraySchema = new JsonSchema();
        arraySchema.setSpecVersion(SpecVersion.V31);

        arraySchema.setTypes(getArrayType());
        Schema<Object> objectSchema = new JsonSchema();
        objectSchema.setSpecVersion(SpecVersion.V31);
        objectSchema.setTypes(getObjectType());

        Map<String, Schema> properties = new HashMap<>();
        Schema<Object> childArraySchema = new JsonSchema();
        childArraySchema.setSpecVersion(SpecVersion.V31);
        childArraySchema.setTypes(getArrayType());
        Schema<Object> refSchema = new JsonSchema();
        refSchema.setSpecVersion(SpecVersion.V31);
        refSchema.set$ref(OASSchemaProcessorConstants.REF_2);
        childArraySchema.setItems(refSchema);
        properties.put(OASSchemaProcessorConstants.CVV, childArraySchema);

        objectSchema.setProperties(properties);
        arraySchema.setItems(objectSchema);

        nestedProperties.put(OASSchemaProcessorConstants.CARD_DETAILS, arraySchema);
        nestedArrayObjectSchema.setProperties(nestedProperties);
        return nestedArrayObjectSchema;
    }

    private Schema<Object> getOneOfSchemaWithRefArray() {
        Schema<Object> cardSchema = new JsonSchema();
        cardSchema.setSpecVersion(SpecVersion.V31);
        Schema<Object> ref1 = new JsonSchema();
        ref1.setSpecVersion(SpecVersion.V31);
        ref1.set$ref(OASSchemaProcessorConstants.REF_1);
        Schema<Object> ref2 = new JsonSchema();
        ref2.setSpecVersion(SpecVersion.V31);
        ref2.set$ref(OASSchemaProcessorConstants.REF_2);
        List<Schema> oneOfList = new ArrayList<>();
        oneOfList.add(ref1);
        oneOfList.add(ref2);
        cardSchema.setOneOf(oneOfList);
        return cardSchema;
    }

    private Schema<Object> getArraySchemaWithComposedRefs() {
        Schema<Object> arrayschema = new JsonSchema();
        arrayschema.setSpecVersion(SpecVersion.V31);
        arrayschema.setTypes(getArrayType());
        Schema<Object> composedItems = new JsonSchema();
        composedItems.setSpecVersion(SpecVersion.V31);

        List<Schema> anyOfList = new ArrayList<>();
        anyOfList.add(new Schema().$ref(OASSchemaProcessorConstants.REF_1));
        List<Schema> oneOfList = new ArrayList<>();
        oneOfList.add(new Schema().$ref(OASSchemaProcessorConstants.REF_2));
        oneOfList.add(new Schema().$ref(OASSchemaProcessorConstants.REF_3));

        composedItems.setAnyOf(anyOfList);
        composedItems.setOneOf(oneOfList);
        arrayschema.setItems(composedItems);
        return arrayschema;
    }

    private Schema<Object> getNestedArrayInsideOneOf() {
        Schema<Object> oneOfSchema = new JsonSchema();
        oneOfSchema.setSpecVersion(SpecVersion.V31);
        List<Schema> oneOfList = new ArrayList<>();
        Schema<Object> refSchema = new JsonSchema();
        refSchema.set$ref(OASSchemaProcessorConstants.REF_1);
        Schema<Object> arraySchema2 = new JsonSchema();
        arraySchema2.setTypes(getArrayType());
        arraySchema2.setItems(refSchema);

        Schema<Object> arraySchema1 = new JsonSchema();
        arraySchema1.setTypes(getArrayType());
        arraySchema1.setItems(arraySchema2);

        Schema<Object> arraySchema3 = new JsonSchema();
        arraySchema3.setTypes(getArrayType());
        Schema<Object> intSchema = new JsonSchema();
        intSchema.setTypes(getStringType());
        arraySchema3.setItems(intSchema);

        oneOfList.add(arraySchema1);
        oneOfList.add(arraySchema3);
        oneOfSchema.setOneOf(oneOfList);
        return oneOfSchema;
    }

    private Schema<Object> getArraySchemaWithRefMap() {
        Schema<Object> arraySchema = new JsonSchema();
        arraySchema.setSpecVersion(SpecVersion.V31);

        arraySchema.setTypes(getArrayType());
        Schema<Object> objectSchema = new JsonSchema();
        objectSchema.setSpecVersion(SpecVersion.V31);
        objectSchema.setTypes(getObjectType());

        Map<String, Schema> properties = new HashMap<>();
        Schema<Object> numberSchema = new JsonSchema();
        numberSchema.setSpecVersion(SpecVersion.V31);
        numberSchema.setTypes(getStringType());
        properties.put(OASSchemaProcessorConstants.NUMBER, numberSchema);

        Schema<Object> expiryDateSchema = new JsonSchema();
        expiryDateSchema.setSpecVersion(SpecVersion.V31);
        expiryDateSchema.setTypes(getStringType());
        expiryDateSchema.setFormat(OASSchemaProcessorConstants.DATE_FORMAT);
        List<Object> examples = new ArrayList<>();
        examples.add(OASSchemaProcessorConstants.EXAMPLE_DATE);
        expiryDateSchema.setExamples(examples);
        properties.put(OASSchemaProcessorConstants.EXPIRY_DATE, expiryDateSchema);

        Schema<Object> cvvSchema = new JsonSchema();
        cvvSchema.setSpecVersion(SpecVersion.V31);
        cvvSchema.set$ref(OASSchemaProcessorConstants.REF_1);
        properties.put(OASSchemaProcessorConstants.CVV, cvvSchema);

        objectSchema.setProperties(properties);
        arraySchema.setItems(objectSchema);
        return arraySchema;
    }

    private Schema<Object> getMapSchemaWithAdditionalProperties() {
        Schema<Object> mapSchema = new JsonSchema();
        mapSchema.setSpecVersion(SpecVersion.V31);
        mapSchema.setTypes(getObjectType());

        Schema<Object> additionalPropertySchema = new JsonSchema();
        additionalPropertySchema.setSpecVersion(SpecVersion.V31);
        additionalPropertySchema.setTypes(getStringType());
        Map<String, String> exampleMap = new HashMap<>();
        exampleMap.put(OASSchemaProcessorConstants.VERSION, OASSchemaProcessorConstants.EXAMPLE_VERSION);
        exampleMap.put(OASSchemaProcessorConstants.ENVIRONMENT, OASSchemaProcessorConstants.EXAMPLE_ENVIRONMENT);
        additionalPropertySchema.setExample(exampleMap);
        mapSchema.setAdditionalProperties(additionalPropertySchema);

        Schema<Object> refSchema = new JsonSchema();
        refSchema.setSpecVersion(SpecVersion.V31);
        refSchema.set$ref(OASSchemaProcessorConstants.REF_1);

        Schema<Object> descriptionSchema = new JsonSchema();
        descriptionSchema.setSpecVersion(SpecVersion.V31);
        Set<String> stringNullableType = getStringType();
        stringNullableType.add(APISpecParserConstants.OPENAPIV31_SCHEMA_TYPE_NULLABLE);
        descriptionSchema.setTypes(stringNullableType);
        descriptionSchema.setExample(OASSchemaProcessorConstants.SAMPLE_DESCRIPTION);

        Map<String, Schema> properties = new HashMap<>();
        properties.put(OASSchemaProcessorConstants.CARD_DETAILS, refSchema);
        properties.put(OASSchemaProcessorConstants.DESCRIPTION, descriptionSchema);
        mapSchema.setProperties(properties);

        return mapSchema;
    }

    private Schema<Object> getAllOfSchemaWithRef() {
        Schema<Object> allOfSchema = new JsonSchema();
        allOfSchema.setSpecVersion(SpecVersion.V31);

        Schema<Object> baseResponseSchema = new JsonSchema();
        baseResponseSchema.set$ref(OASSchemaProcessorConstants.REF_2);

        Schema<Object> objectSchema = new JsonSchema();
        objectSchema.setSpecVersion(SpecVersion.V31);
        objectSchema.setTypes(getObjectType());

        Schema<Object> cardIdSchema = new JsonSchema();
        cardIdSchema.setSpecVersion(SpecVersion.V31);
        cardIdSchema.setTypes(getStringType());
        List<Object> examples = new ArrayList<>();
        examples.add(OASSchemaProcessorConstants.EXAMPLE_CARD_ID);
        cardIdSchema.setExamples(examples);

        Schema<Object> cardDetailsSchema = new JsonSchema();
        cardDetailsSchema.setSpecVersion(SpecVersion.V31);
        cardDetailsSchema.set$ref(OASSchemaProcessorConstants.REF_3);

        Map<String, Schema> properties = new HashMap<>();
        properties.put(OASSchemaProcessorConstants.CARD_ID, cardIdSchema);
        properties.put(OASSchemaProcessorConstants.CARD_DETAILS, cardDetailsSchema);
        objectSchema.setProperties(properties);

        List<Schema> allOfList = new ArrayList<>();
        allOfList.add(baseResponseSchema);
        allOfList.add(objectSchema);
        allOfSchema.setAllOf(allOfList);
        return allOfSchema;
    }

    private Schema<Object> getOneOfSchemaWithRef() {
        Schema<Object> cardTypeSchema = new JsonSchema();
        cardTypeSchema.setSpecVersion(SpecVersion.V31);

        Schema<Object> visaMastercardSchema = new JsonSchema();
        visaMastercardSchema.setSpecVersion(SpecVersion.V31);
        visaMastercardSchema.setTypes(getStringType());
        visaMastercardSchema.setEnum(Arrays.asList(OASSchemaProcessorConstants.EXAMPLE_CARD_TYPE_1,
                OASSchemaProcessorConstants.EXAMPLE_CARD_TYPE_3));

        Schema<Object> amexSchema = new JsonSchema();
        amexSchema.setSpecVersion(SpecVersion.V31);
        amexSchema.setTypes(getStringType());
        amexSchema.setEnum(Collections.singletonList(OASSchemaProcessorConstants.EXAMPLE_CARD_TYPE_2));

        Schema<Object> baseResponseSchema = new JsonSchema();
        baseResponseSchema.set$ref(OASSchemaProcessorConstants.REF_2);

        List<Schema> oneOfList = new ArrayList<>();
        oneOfList.add(visaMastercardSchema);
        oneOfList.add(amexSchema);
        oneOfList.add(baseResponseSchema);
        cardTypeSchema.setOneOf(oneOfList);
        List<Object> examples = new ArrayList<>();
        examples.add(OASSchemaProcessorConstants.EXAMPLE_CARD_TYPE_1);
        cardTypeSchema.setExamples(examples);
        return cardTypeSchema;
    }

    private Schema<Object> getAnyOfSchemaWithRef() {
        Schema<Object> anyOfSchema = new JsonSchema();
        anyOfSchema.setSpecVersion(SpecVersion.V31);

        Schema<Object> baseResponseSchema = new JsonSchema();
        baseResponseSchema.set$ref(OASSchemaProcessorConstants.REF_2);

        Schema<Object> objectSchema = new JsonSchema();
        objectSchema.setSpecVersion(SpecVersion.V31);
        objectSchema.setTypes(getObjectType());

        Schema<Object> cardIdSchema = new JsonSchema();
        cardIdSchema.setSpecVersion(SpecVersion.V31);
        cardIdSchema.setTypes(getStringType());
        List<Object> examples = new ArrayList<>();
        examples.add(OASSchemaProcessorConstants.EXAMPLE_CARD_ID);
        cardIdSchema.setExamples(examples);

        Schema<Object> cardDetailsSchema = new JsonSchema();
        cardDetailsSchema.setSpecVersion(SpecVersion.V31);
        cardDetailsSchema.set$ref(OASSchemaProcessorConstants.REF_3);

        Map<String, Schema> properties = new HashMap<>();
        properties.put(OASSchemaProcessorConstants.CARD_ID, cardIdSchema);
        properties.put(OASSchemaProcessorConstants.CARD_DETAILS, cardDetailsSchema);
        objectSchema.setProperties(properties);

        List<Schema> anyOfList = new ArrayList<>();
        anyOfList.add(baseResponseSchema);
        anyOfList.add(objectSchema);
        anyOfSchema.setAnyOf(anyOfList);
        return anyOfSchema;
    }

    private Schema<Object> getNestedComposedSchema() {
        Schema<Object> nestedSchema = new JsonSchema();
        nestedSchema.setSpecVersion(SpecVersion.V31);

        Schema<Object> firstOneOf = new JsonSchema();
        firstOneOf.setSpecVersion(SpecVersion.V31);
        firstOneOf.setTypes(getStringType());

        Schema<Object> secondOneOf = new JsonSchema();
        secondOneOf.setSpecVersion(SpecVersion.V31);
        secondOneOf.setTypes(getObjectType());
        Map<String, Schema> secondOneOfProps = new HashMap<>();
        Schema<Object> anyOfSchema = getAnyOfSchemaWithRef();
        secondOneOfProps.put(OASSchemaProcessorConstants.ANY_OF_PROPERTY, anyOfSchema);
        secondOneOf.setProperties(secondOneOfProps);

        List<Schema> oneOfList = new ArrayList<>();
        oneOfList.add(firstOneOf);
        oneOfList.add(secondOneOf);
        nestedSchema.setOneOf(oneOfList);
        return nestedSchema;
    }

    private Schema<Object> getSimpleSchema() {
        Schema<Object> stringTypeSchema = new JsonSchema();
        stringTypeSchema.setSpecVersion(SpecVersion.V31);
        Set<String> stringType = getStringType();
        stringType.add(APISpecParserConstants.OPENAPIV31_SCHEMA_TYPE_NULLABLE);
        stringTypeSchema.setTypes(stringType);
        List<Object> examples = new ArrayList<>();
        examples.add(OASSchemaProcessorConstants.EXAMPLE_CARD_ID);
        stringTypeSchema.setExamples(examples);
        return stringTypeSchema;
    }

    private Schema<Object> getComplexComposedObjectSchema() {
        Schema<Object> complexSchema = new JsonSchema();
        complexSchema.setSpecVersion(SpecVersion.V31);
        complexSchema.setTypes(getObjectType());

        Schema<Object> composedObjectSchema = new JsonSchema();
        composedObjectSchema.setSpecVersion(SpecVersion.V31);
        composedObjectSchema.setTypes(getObjectType());

        Schema<Object> firstObject = new JsonSchema();
        firstObject.setSpecVersion(SpecVersion.V31);
        firstObject.setTypes(getObjectType());
        firstObject.addProperty(OASSchemaProcessorConstants.CARD_DETAILS, new JsonSchema()
                .$ref(OASSchemaProcessorConstants.REF_3));
        firstObject.addProperty(OASSchemaProcessorConstants.CARD_ID,
                new JsonSchema().types(getStringType()).examples(getExamples()));

        Schema<Object> secondObject = new JsonSchema();
        secondObject.setSpecVersion(SpecVersion.V31);
        secondObject.set$ref(OASSchemaProcessorConstants.REF_1);

        List<Schema> allOfList = new ArrayList<>();
        allOfList.add(firstObject);
        allOfList.add(secondObject);
        composedObjectSchema.setAllOf(allOfList);

        Schema<Object> arrayPropertySchema = getArraySchemaWithRefMap();

        Schema<Object> anotherObjectSchema = new JsonSchema();
        anotherObjectSchema.setSpecVersion(SpecVersion.V31);
        anotherObjectSchema.set$ref(OASSchemaProcessorConstants.REF_2);

        // Adding anotherObject to the main schema
        complexSchema.addProperty(OASSchemaProcessorConstants.COMPOSED_OBJECT, composedObjectSchema);
        complexSchema.addProperty(OASSchemaProcessorConstants.ARRAY_PROPERTY, arrayPropertySchema);
        complexSchema.addProperty(OASSchemaProcessorConstants.ANOTHER_OBJECT, anotherObjectSchema);
        return complexSchema;
    }

    private Set<String> getObjectType() {
        Set<String> types = new HashSet<>();
        types.add(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE);
        return types;
    }

    private Set<String> getStringType() {
        Set<String> types = new HashSet<>();
        types.add(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE);
        return types;
    }

    private Set<String> getArrayType() {
        Set<String> types = new HashSet<>();
        types.add(APISpecParserConstants.OPENAPI_ARRAY_DATA_TYPE);
        return types;
    }

    private List<Object> getExamples() {
        List<Object> examples = new ArrayList<>();
        examples.add(OASSchemaProcessorConstants.EXAMPLE_CARD_ID);
        return examples;
    }
}
