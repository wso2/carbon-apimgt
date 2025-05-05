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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OAS30ProcessorTest {

    private OpenAPI30SchemaProcessor processor;

    @Before
    public void setUp() {
        processor = new OpenAPI30SchemaProcessor();  // Create the real object of OAS31To30Processor
    }

    @Test
    public void convertSchemaTest() {
        Schema<Object> schema = new Schema<>();
        schema.setExample(OASSchemaProcessorConstants.EXAMPLE);
        schema.setType(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE);
        schema.setNullable(true);
        processor.convertSchema(schema);
        Assert.assertEquals(OASSchemaProcessorConstants.EXAMPLE, schema.getExample());
        Assert.assertNull(schema.getExamples());
        Assert.assertNull(schema.getTypes());
        Assert.assertEquals(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE, schema.getType());
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
        Assert.assertEquals(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.SCHEMAS).size(), 1);
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.SCHEMAS)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.REF_1)));
    }

    @Test
    public void extractReferenceFromAllOfSchemaTest() {
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> allOfSchema = getAllOfSchemaWithRef();
        processor.extractReferenceFromSchema(allOfSchema, context);
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
        Assert.assertEquals(1, context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.SCHEMAS).size());
        Assert.assertTrue(context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.SCHEMAS)
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
    public void extractReferenceFromComposedArrayWithRef() {
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> oneOfSchema = getArraySchemaWithComposedRefs();
        processor.extractReferenceFromSchema(oneOfSchema, context);
        Assert.assertEquals(3, context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.SCHEMAS).size());
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
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.SCHEMAS).isEmpty());
    }

    @Test
    public void extractReferenceFromComplexObjectSchemaTest() {
        //Object with ref, array with ref and allOf composed object (object with ref and direct ref prop).
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> schema = getComplexComposedObjectSchema();
        processor.extractReferenceFromSchema(schema, context);
        Map<String, Schema> properties = schema.getProperties();
        Schema<Object> composedObj = properties.get(OASSchemaProcessorConstants.COMPOSED_OBJECT);
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
        assertAnyOfSchemaWithRef(context);
    }

    @Test
    public void extractReferenceFromNestedArrayInsideOneOfSchema() {
        //oneOf with nested arrays
        OASParserUtil.SwaggerUpdateContext context = new OASParserUtil.SwaggerUpdateContext();
        Schema<Object> schema = getNestedArrayInsideOneOf();
        processor.extractReferenceFromSchema(schema, context);
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
        Assert.assertEquals(1, context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.SCHEMAS).size());
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.SCHEMAS)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.REF_2)));
    }

    private Schema<Object> getNestedArrayInsideOneOf() {
        Schema<Object> oneOfSchema = new ComposedSchema();
        oneOfSchema.setSpecVersion(SpecVersion.V30);
        List<Schema> oneOfList = new ArrayList<>();
        Schema<Object> refSchema = new Schema<>();
        refSchema.set$ref(OASSchemaProcessorConstants.REF_1);
        Schema<Object> arraySchema2 = new ArraySchema();
        arraySchema2.setType(APISpecParserConstants.OPENAPI_ARRAY_DATA_TYPE);
        arraySchema2.setItems(refSchema);

        Schema<Object> arraySchema1 = new ArraySchema();
        arraySchema1.setType(APISpecParserConstants.OPENAPI_ARRAY_DATA_TYPE);
        arraySchema1.setItems(arraySchema2);

        Schema<Object> arraySchema3 = new ArraySchema();
        arraySchema3.setType(APISpecParserConstants.OPENAPI_ARRAY_DATA_TYPE);
        Schema<String> intSchema = new StringSchema();
        intSchema.setType(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE);
        arraySchema3.setItems(intSchema);

        oneOfList.add(arraySchema1);
        oneOfList.add(arraySchema3);
        oneOfSchema.setOneOf(oneOfList);
        return oneOfSchema;
    }

    private Schema<Object> getMapSchemaWithAdditionalProperties() {
        Schema<Object> mapSchema = new MapSchema();
        mapSchema.setSpecVersion(SpecVersion.V30);
        mapSchema.setType(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE);

        Schema<String> additionalPropertySchema = new StringSchema();
        additionalPropertySchema.setSpecVersion(SpecVersion.V30);
        additionalPropertySchema.setType(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE);
        mapSchema.setAdditionalProperties(additionalPropertySchema);

        Schema<Object> refSchema = new Schema<>();
        refSchema.setSpecVersion(SpecVersion.V30);
        refSchema.set$ref(OASSchemaProcessorConstants.REF_1);

        Schema<String> descriptionSchema = new StringSchema();
        descriptionSchema.setSpecVersion(SpecVersion.V30);
        descriptionSchema.setType(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE);
        descriptionSchema.setNullable(true);

        Map<String, Schema> properties = new HashMap<>();
        properties.put(OASSchemaProcessorConstants.CARD_DETAILS, refSchema);
        properties.put(OASSchemaProcessorConstants.DESCRIPTION, descriptionSchema);
        mapSchema.setProperties(properties);

        return mapSchema;
    }

    private Schema<Object> getNestedMapSchemaWithAdditionalProperties() {
        Schema<Object> parentSchema = new ObjectSchema();
        parentSchema.setType(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE);

        Schema<String> descriptionSchema = new StringSchema();
        descriptionSchema.setSpecVersion(SpecVersion.V30);
        descriptionSchema.setType(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE);
        descriptionSchema.setNullable(true);

        Schema<Object> refSchema = new Schema<>();
        refSchema.setSpecVersion(SpecVersion.V30);
        refSchema.set$ref(OASSchemaProcessorConstants.REF_1);

        Schema<Object> nestedSchema = new MapSchema();
        nestedSchema.setSpecVersion(SpecVersion.V30);
        nestedSchema.setType(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE);

        Schema<Object> additionalPropertySchema = new MapSchema();
        additionalPropertySchema.setSpecVersion(SpecVersion.V30);
        additionalPropertySchema.setType(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE);
        additionalPropertySchema.setAdditionalProperties(new Schema<>().$ref(OASSchemaProcessorConstants.REF_2));
        nestedSchema.setAdditionalProperties(additionalPropertySchema);

        Map<String, Schema> properties = new HashMap<>();
        properties.put(OASSchemaProcessorConstants.CARD_DETAILS, refSchema);
        properties.put(OASSchemaProcessorConstants.DESCRIPTION, descriptionSchema);
        properties.put(OASSchemaProcessorConstants.VERSION, nestedSchema);
        parentSchema.setProperties(properties);
        return parentSchema;
    }


    private Schema<Object> getOneOfSchemaWithRef() {
        Schema<Object> cardTypeSchema = new ComposedSchema();
        cardTypeSchema.setSpecVersion(SpecVersion.V30);

        Schema<String> visaMastercardSchema = new StringSchema();
        visaMastercardSchema.setSpecVersion(SpecVersion.V30);
        visaMastercardSchema.setType(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE);
        visaMastercardSchema.setEnum(Arrays.asList(OASSchemaProcessorConstants.EXAMPLE_CARD_TYPE_1,
                OASSchemaProcessorConstants.EXAMPLE_CARD_TYPE_3));

        Schema<String> amexSchema = new StringSchema();
        amexSchema.setSpecVersion(SpecVersion.V30);
        amexSchema.setType(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE);
        amexSchema.setEnum(Collections.singletonList(OASSchemaProcessorConstants.EXAMPLE_CARD_TYPE_2));

        Schema<Object> baseResponseSchema = new Schema<>();
        baseResponseSchema.set$ref(OASSchemaProcessorConstants.REF_2);

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
        ref1.set$ref(OASSchemaProcessorConstants.REF_1);
        Schema<Object> ref2 = new Schema<>();
        ref2.set$ref(OASSchemaProcessorConstants.REF_2);
        List<Schema> oneOfList = new ArrayList<>();
        oneOfList.add(ref1);
        oneOfList.add(ref2);
        cardSchema.setOneOf(oneOfList);
        return cardSchema;
    }

    private Schema<Object> getArraySchemaWithComposedRefs() {
        Schema<Object> arrayschema = new ArraySchema();
        arrayschema.setSpecVersion(SpecVersion.V30);
        arrayschema.setType(APISpecParserConstants.OPENAPI_ARRAY_DATA_TYPE);
        Schema<Object> composedItems = new ComposedSchema();

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

    private Schema<Object> getNestedArraySchemaWithRef() {
        Schema<Object> nestedArrayObjectSchema = new ObjectSchema();
        nestedArrayObjectSchema.setSpecVersion(SpecVersion.V30);
        nestedArrayObjectSchema.setType(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE);
        Map<String, Schema> nestedProperties = new HashMap<>();

        Schema<Object> arraySchema = new ArraySchema();
        arraySchema.setSpecVersion(SpecVersion.V30);

        arraySchema.setType(APISpecParserConstants.OPENAPI_ARRAY_DATA_TYPE);
        Schema<Object> objectSchema = new ObjectSchema();
        objectSchema.setSpecVersion(SpecVersion.V30);
        objectSchema.setType(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE);

        Map<String, Schema> properties = new HashMap<>();
        Schema<Object> childArraySchema = new ArraySchema();
        childArraySchema.setSpecVersion(SpecVersion.V30);
        childArraySchema.setType(APISpecParserConstants.OPENAPI_ARRAY_DATA_TYPE);
        Schema<Object> refSchema = new ObjectSchema();
        refSchema.setSpecVersion(SpecVersion.V30);
        refSchema.set$ref(OASSchemaProcessorConstants.REF_2);
        childArraySchema.setItems(refSchema);
        properties.put(OASSchemaProcessorConstants.CVV, childArraySchema);

        objectSchema.setProperties(properties);
        arraySchema.setItems(objectSchema);

        nestedProperties.put(OASSchemaProcessorConstants.CARD_DETAILS, arraySchema);
        nestedArrayObjectSchema.setProperties(nestedProperties);
        return nestedArrayObjectSchema;
    }

    private Schema<Object> getArraySchemaWithRefMap() {
        Schema<Object> arraySchema = new ArraySchema();
        arraySchema.setSpecVersion(SpecVersion.V30);

        arraySchema.setType(APISpecParserConstants.OPENAPI_ARRAY_DATA_TYPE);
        Schema<Object> objectSchema = new JsonSchema();
        objectSchema.setSpecVersion(SpecVersion.V30);
        objectSchema.setType(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE);

        Map<String, Schema> properties = new HashMap<>();
        Schema<String> numberSchema = new StringSchema();
        numberSchema.setSpecVersion(SpecVersion.V30);
        numberSchema.setType(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE);
        properties.put(OASSchemaProcessorConstants.NUMBER, numberSchema);

        Schema<String> expiryDateSchema = new StringSchema();
        expiryDateSchema.setSpecVersion(SpecVersion.V30);
        expiryDateSchema.setType(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE);
        expiryDateSchema.setFormat(OASSchemaProcessorConstants.DATE_FORMAT);
        properties.put(OASSchemaProcessorConstants.EXPIRY_DATE, expiryDateSchema);

        Schema<Object> cvvSchema = new Schema<>();
        cvvSchema.setSpecVersion(SpecVersion.V30);
        cvvSchema.set$ref(OASSchemaProcessorConstants.REF_1);
        properties.put(OASSchemaProcessorConstants.CVV, cvvSchema);

        objectSchema.setProperties(properties);
        arraySchema.setItems(objectSchema);
        return arraySchema;
    }

    private Schema<Object> getNestedComposedSchema() {
        Schema<Object> nestedSchema = new ComposedSchema();
        nestedSchema.setSpecVersion(SpecVersion.V30);

        Schema<String> firstOneOf = new StringSchema();
        firstOneOf.setSpecVersion(SpecVersion.V30);
        firstOneOf.setType(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE);

        Schema<Object> secondOneOf = new ObjectSchema();
        secondOneOf.setSpecVersion(SpecVersion.V30);
        secondOneOf.setType(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE);
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

    private Schema<Object> getComplexComposedObjectSchema() {
        Schema<Object> complexSchema = new ObjectSchema();
        complexSchema.setSpecVersion(SpecVersion.V30);
        complexSchema.setType(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE);

        Schema<Object> composedObjectSchema = new ComposedSchema();
        composedObjectSchema.setSpecVersion(SpecVersion.V30);
        composedObjectSchema.setType(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE);

        Schema<Object> firstObject = new ObjectSchema();
        firstObject.setSpecVersion(SpecVersion.V30);
        firstObject.setType(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE);
        firstObject.addProperty(OASSchemaProcessorConstants.CARD_DETAILS, new Schema()
                .$ref(OASSchemaProcessorConstants.REF_3));
        firstObject.addProperty(OASSchemaProcessorConstants.CARD_ID,
                new StringSchema().type(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE));

        Schema<Object> secondObject = new Schema<>();
        secondObject.setSpecVersion(SpecVersion.V30);
        secondObject.set$ref(OASSchemaProcessorConstants.REF_1);

        List<Schema> allOfList = new ArrayList<>();
        allOfList.add(firstObject);
        allOfList.add(secondObject);
        composedObjectSchema.setAllOf(allOfList);

        Schema<Object> arrayPropertySchema = getArraySchemaWithRefMap();

        Schema<Object> anotherObjectSchema = new Schema<>();
        anotherObjectSchema.setSpecVersion(SpecVersion.V30);
        anotherObjectSchema.set$ref(OASSchemaProcessorConstants.REF_2);

        // Adding anotherObject to the main schema
        complexSchema.addProperty(OASSchemaProcessorConstants.COMPOSED_OBJECT, composedObjectSchema);
        complexSchema.addProperty(OASSchemaProcessorConstants.ARRAY_PROPERTY, arrayPropertySchema);
        complexSchema.addProperty(OASSchemaProcessorConstants.ANOTHER_OBJECT, anotherObjectSchema);
        return complexSchema;
    }

    private Schema<String> getSimpleSchema() {
        Schema<String> stringTypeSchema = new StringSchema();
        stringTypeSchema.setSpecVersion(SpecVersion.V30);
        stringTypeSchema.setType(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE);
        return stringTypeSchema;
    }

    private Schema<Object> getAnyOfSchemaWithRef() {
        Schema<Object> anyOfSchema = new ComposedSchema();
        anyOfSchema.setSpecVersion(SpecVersion.V30);

        Schema<Object> baseResponseSchema = new Schema<>();
        baseResponseSchema.set$ref(OASSchemaProcessorConstants.REF_2);

        Schema<Object> objectSchema = new ObjectSchema();
        objectSchema.setSpecVersion(SpecVersion.V30);
        objectSchema.setType(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE);

        Schema<String> cardIdSchema = new StringSchema();
        cardIdSchema.setSpecVersion(SpecVersion.V30);
        cardIdSchema.setType(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE);

        Schema<Object> cardDetailsSchema = new Schema<>();
        cardDetailsSchema.setSpecVersion(SpecVersion.V30);
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

    private Schema<Object> getAllOfSchemaWithRef() {
        Schema<Object> allOfSchema = new ComposedSchema();
        allOfSchema.setSpecVersion(SpecVersion.V30);

        Schema<Object> baseResponseSchema = new Schema<>();
        baseResponseSchema.set$ref(OASSchemaProcessorConstants.REF_2);

        Schema<Object> objectSchema = new ObjectSchema();
        objectSchema.setSpecVersion(SpecVersion.V30);
        objectSchema.setType(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE);

        Schema<String> cardIdSchema = new StringSchema();
        cardIdSchema.setSpecVersion(SpecVersion.V30);
        cardIdSchema.setType(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE);

        Schema<Object> cardDetailsSchema = new Schema<>();
        cardDetailsSchema.setSpecVersion(SpecVersion.V30);
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

    private void assertAnyOfSchemaWithRef(OASParserUtil.SwaggerUpdateContext context) {
        Assert.assertEquals(2, context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.SCHEMAS).size());
        Assert.assertTrue(context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.SCHEMAS)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.REF_2)));
        Assert.assertTrue(context.getReferenceObjectMapping()
                .get(OASSchemaProcessorConstants.SCHEMAS)
                .contains(OASParserUtil.getRefKey(OASSchemaProcessorConstants.REF_3)));
    }

    private void assertArraySchema(Schema<Object> arraySchema, OASParserUtil.SwaggerUpdateContext context) {
        Assert.assertEquals(APISpecParserConstants.OPENAPI_ARRAY_DATA_TYPE, arraySchema.getType());
        Assert.assertEquals(APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE, arraySchema.getItems().getType());
        Assert.assertEquals(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE, arraySchema.getItems().getProperties()
                .get(OASSchemaProcessorConstants.NUMBER).getType());
        Assert.assertEquals(APISpecParserConstants.OPENAPI_STRING_DATA_TYPE, arraySchema.getItems().getProperties()
                .get(OASSchemaProcessorConstants.EXPIRY_DATE).getType());
        Assert.assertTrue(context.getReferenceObjectMapping().get(OASSchemaProcessorConstants.SCHEMAS).contains(
                OASParserUtil.getRefKey(OASSchemaProcessorConstants.REF_1)));
    }
}
