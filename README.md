# DomainDrivenDesign-ktlint-rules

Here is the set of annotations that you will need to add to your project.

If you skip that step, you will get ktlint errors for each annotation you are missing

```kotlin
package myproject

import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

object DomainDrivenDesignAnnotations {
    /**
     * The following role is the intention revealing code of the API.
     * It must be written in declarative design and cannot be a facade to the code.
     * The abstraction level must be respected in order to let the action
     * describe the interaction end to end.
     *
     * The action can only have a constructor and a public method, 0 private method
     */
    @Component
    annotation class Action

    /**
     * The following role is an orchestrator of several Repositories, Gateways
     * and computation. It can act as a facility to tackle the complexity
     * of the business logic that the action class cannot describe without
     * running the risk of becoming too difficult to read.
     *
     * The domain service can have several public methods, however we strongly
     * recommend to make all those methods semantically related to the same
     * use case
     */
    @Service
    annotation class DomainService

    /**
     * The following role is a caller to an external / foreign service
     * You will find a SOAP/REST api call (and only one)
     *
     * Only one API call per Gateway is allowed, unless if the foreign API
     * has different methods to get the same value
     *
     * A gateway must take in parameters Aggregates, Entities Or Value Types,
     * and must return Aggregates, Entities or Value Types
     */
    @Repository
    annotation class Gateway

    /**
     * The following role is a caller to an external / foreign database
     * There is either one query execution or one call to a stored procedure
     *
     * All the needed information must be passed in the signature.
     * The component should not do any other data fetch operation.
     */
    // Repository is already declared in spring, no need to redeclare

    /**
     * Indicates that the following role can welcome incoming requests
     * from a consuming actor. The Endpoint can have different shapes :
     * - a message consumer
     * - an api controller,
     * - a graphql query / mutation
     * - an actor (akka streams)
     * - a bolt (apache storm)
     * An endpoint can only be located in the infra package.
     * An endpoint should only convert a technical signature into a call
     * to an action, and then extract the appropriate information from
     * the return of the action to the consumer.
     **/
    @Component
    annotation class Endpoint

    /**
     * The following data structure belongs to a model that is not part of
     * this domain model, because its structure is not compatible.
     *
     * To avoid that case, we can try to reuse the value types, entities and
     * aggregates from the domain model in the repositories or gateways.
     *
     * Which is not possible all the time because of the interface contracts.
     *
     * This means that the data structure must be hold inside the infra package,
     * and should not be read or written by the actions and domain services.
     *
     * The foreign model must come with a converter
     *
     *    ____________________________________________________________________
     * ⚠ | you are not allowed to pull any of these roles in the domain     |
     *   | package, neither in an Action or a DomainService                 |
     *   -------------------------------------------------------------------
     */
    annotation class ForeignModel

    /**
     * The following data structure is the root information to pass in an action
     * or to be returned from an action.
     *
     * Only composite data structures can be Aggregate, and an aggregate
     * cannot be a value type unless if the action has no access to
     * Gateways and Repositories
     */
    annotation class Aggregate

    /**
     * Indicates that the following data structure has no reference
     * and cannot identify any repository value
     */
    annotation class ValueType

    /**
     * Indicates that the following data structure contains at least one reference
     * in a storage either in this project or in a foreign service
     * Therefore must be considered as globally unique and identifiable in the system
     */
    annotation class Entity
}
```
