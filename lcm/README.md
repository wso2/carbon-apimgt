# Lifecycle Management Component

This component provides lifecycle management capability to any of the resource type which requires lifecycle capability.
This will store all the data related to lifecycles in its side. Component will provide an unique id to outside to
maintain tha mapping between the lifecycle data external object (For ex: API, APP, REST Service)

All the operations are exposed the through the "ManagedLifecycle" interface. Any object which require lifecycle only
need to implement this interface and invoke relevant operations.
This interface have following methods with default implementation.

* void createLifecycleEntry(String lcName, String user) throws LifecycleException
* LifecycleState executeLifecycleEvent(String targetState, String user, String lcName)
              throws LifecycleException
* void removeLifecycleEntry(String lcName) throws LifecycleException
* LifecycleState checkListItemEvent(String lcName, String checkListItemName, boolean value)
* LifecycleState getCurrentLifecycleState(String lcName) throws LifecycleException

Following methods should be provide with implementation.

* void associateLifecycle(LifecycleState lifecycleState) throws LifecycleException
    After adding lifecycle to object(which implements the interface) by calling "createLifecycleEntry" should set this
    lifecycle state object to its instance. Then they should implement the logic to persist lifecycle id which is in
    the lifecycle state. This id is the mapping between particular object and its lifecycle data.

* void dissociateLifecycle() throws LifecycleException
    This method should update its the current object lifecycle state to null and implement the logic to remove the
    persisted lifecycle id which was implemented in "associateLifecycle" method.

* String getLifecycleId(String lcName)
    This method should provide implementation to give the lifecycle id when lifecycle name is provided. A map can be
    used to maintain the mapping. This is used if one object is associated with multiple lifecycles.

#### Note
    Please note that any of the above 3 methods should not be called from outside. The default methods in the
    interface only call the above 3 methods.
    Refer the SampleApi class for an implementation for "ManagedLifecycle" interface.


