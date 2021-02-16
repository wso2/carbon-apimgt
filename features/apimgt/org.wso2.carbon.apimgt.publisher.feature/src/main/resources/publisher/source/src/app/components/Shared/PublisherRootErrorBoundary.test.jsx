/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import React from 'react';
import { createShallow } from '@material-ui/core/test-utils';
import PublisherRootErrorBoundary from './PublisherRootErrorBoundary';

describe('PublisherRootErrorBoundary test', () => {
    test('Should return the child element when no exception is thrown', () => {
        const shallow = createShallow();
        const Child = <div>Testing child</div>;
        const Test = <PublisherRootErrorBoundary classes={{}}>{Child}</PublisherRootErrorBoundary>;
        const shallowRendered = shallow(Test);
        expect(shallowRendered.contains(Child)).toBeTruthy();
    });

    test.skip('should return error boundary HTML without any Material UI stylings', () => {
        const message = 'Error boundary test error';
        const TestError = () => {
            throw new Error(message);
        };
        const TestComponent = (
            <PublisherRootErrorBoundary>
                <TestError />
            </PublisherRootErrorBoundary>
        );

        const wrapper = mount(TestComponent);

        const renderedAppErrorBoundary = wrapper.find(PublisherRootErrorBoundary);
        expect(renderedAppErrorBoundary.state().hasError).toBeTruthy();
        expect(renderedAppErrorBoundary.state().error).not.toBeNull();
        expect(renderedAppErrorBoundary.state().error.message).not.toBeNull();
        // Check the error message string
        expect(renderedAppErrorBoundary.state().error.message).toEqual(message);
        expect(renderedAppErrorBoundary.contains('You may refresh the page now or try again later')).toBeTruthy();
    });
});
