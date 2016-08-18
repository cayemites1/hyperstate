package au.com.mountainpass.hyperstate.core.entities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Identifiable;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableSet;

import au.com.mountainpass.hyperstate.client.RepositoryResolver;
import au.com.mountainpass.hyperstate.core.Action;
import au.com.mountainpass.hyperstate.core.Address;
import au.com.mountainpass.hyperstate.core.EntityRelationship;
import au.com.mountainpass.hyperstate.core.JavaAction;
import au.com.mountainpass.hyperstate.core.JavaAddress;
import au.com.mountainpass.hyperstate.core.Link;
import au.com.mountainpass.hyperstate.core.NavigationalRelationship;
import au.com.mountainpass.hyperstate.core.Relationship;

@JsonPropertyOrder({ "class", "properties", "entities", "actions", "links",
        "title" })
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class EntityWrapper<T> extends Entity implements Identifiable<String> {
    private static final int PAGE_SIZE = 10;

    private Map<String, Action<?>> actions = new HashMap<>();

    private Collection<EntityRelationship> entities = null;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @JsonProperty("links")
    private Set<NavigationalRelationship> navigationalRelationships = new HashSet<>();

    private String path;

    T properties;

    private RepositoryResolver resolver;

    protected EntityWrapper(final RepositoryResolver resolver,
            final String path, final T properties, final String title,
            final String... classes) {
        super(title, classes);
        this.resolver = resolver;
        this.properties = properties;
        this.path = path;
        add(new NavigationalRelationship(
                new Link(new JavaAddress(resolver, this), title),
                Relationship.SELF));
        final Method[] methods = this.getClass().getMethods();
        for (final Method method : methods) {
            final HttpMethod httpMethod = JavaAction
                    .determineMethodNature(method);
            if (httpMethod != null) {
                switch (httpMethod) {
                case DELETE:
                    actions.put(method.getName(),
                            new JavaAction<Void>(resolver, this, method));
                    break;
                case POST:
                    actions.put(method.getName(), new JavaAction<CreatedEntity>(
                            resolver, this, method));
                    break;
                case PUT:
                    actions.put(method.getName(), new JavaAction<UpdatedEntity>(
                            resolver, this, method));
                    break;
                case GET:
                    actions.put(method.getName(),
                            new JavaAction<EntityWrapper<?>>(resolver, this,
                                    method));
                    break;
                default:
                }
            }
        }
    }

    protected EntityWrapper(@JsonProperty("properties") final T properties) {
        this.properties = properties;
    }

    public void add(final NavigationalRelationship navigationalRelationship) {
        navigationalRelationships.add(navigationalRelationship);
    }

    public Action<?> getAction(final String name) {
        return actions.get(name);
    }

    @JsonProperty("actions")
    public ImmutableSet<Action<?>> getActions() {
        return ImmutableSet.copyOf(actions.values());
    }

    public CompletableFuture<Collection<EntityRelationship>> getEntities() {
        return getEntities(0);
    }

    public CompletableFuture<Collection<EntityRelationship>> getEntities(
            final int page) {
        if (entities != null) {
            return CompletableFuture.supplyAsync(() -> entities);
        }
        if (resolver != null) {
            return resolver.getRepository().findChildren(this)
                    .thenApplyAsync(results -> {
                        return results.skip(page * PAGE_SIZE).limit(PAGE_SIZE)
                                .collect(Collectors.toList());
                    });
        }
        final List<EntityRelationship> rval = new ArrayList<>();
        return CompletableFuture.supplyAsync(() -> rval);
    }

    @JsonProperty("entities")
    private Collection<EntityRelationship> getEntitiesAndJoin()
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, URISyntaxException {
        return getEntities().join();
    }

    @Override
    @JsonIgnore
    public String getId() {
        return this.path;
    }

    public Link getLink(final String rel) {
        final Optional<NavigationalRelationship> link = getLinks().stream()
                .filter(l -> l.hasRelationship(rel)).findAny();
        if (link.isPresent()) {
            return link.get().getLink();
        } else {
            return null;
        }
    }

    public ImmutableSet<NavigationalRelationship> getLinks() {
        return ImmutableSet.copyOf(navigationalRelationships);
    }

    public T getProperties() {
        return properties;
    }

    @Override
    public <K, L extends EntityWrapper<K>> L reload(final Class<L> type) {
        return (L) this;
    }

    @Override
    public <K, L extends EntityWrapper<K>> L resolve(final Class<L> type) {
        return (L) this;
    }

    @Override
    public <K, L extends EntityWrapper<K>> L resolve(
            final ParameterizedTypeReference<L> type) {
        return (L) this;
    }

    public void setActions(final Action<?>[] actions) {
        for (final Action<?> action : actions) {
            this.actions.put(action.getName(), action);
        }

    }

    public void setEntities(
            final Collection<EntityRelationship> entityRelationships) {
        this.entities = entityRelationships;
    }

    @Override
    public LinkedEntity toLinkedEntity() {
        final LinkedEntity linkedEntity = new LinkedEntity(
                getLink(Relationship.SELF), getTitle(), getClasses());
        return linkedEntity;
    }

    @JsonIgnore
    public RepositoryResolver getResolver() {
        return this.resolver;
    }

    @Override
    @JsonIgnore
    public Address getAddress() {
        return new JavaAddress(resolver, this);
    }

}
