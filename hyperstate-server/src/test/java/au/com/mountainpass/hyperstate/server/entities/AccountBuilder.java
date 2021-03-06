package au.com.mountainpass.hyperstate.server.entities;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import au.com.mountainpass.hyperstate.client.RepositoryResolver;
import au.com.mountainpass.hyperstate.core.EntityRelationship;
import au.com.mountainpass.hyperstate.core.EntityRepository;
import au.com.mountainpass.hyperstate.core.Relationship;

public class AccountBuilder {

    /**
     * 
     */
    private LocalDateTime creationDate;
    private String username;
    private boolean deletable = false;
    private boolean updateable = false;

    public AccountBuilder() {
        // TODO Auto-generated constructor stub
    }

    public CompletableFuture<Account> build(final RepositoryResolver resolver,
            final EntityRepository repository, final String path)
                    throws InterruptedException, ExecutionException {
        AccountProperties properties = new AccountProperties(username,
                creationDate);

        Account entity;
        if (deletable) {
            entity = new AccountWithDelete(repository, properties, path,
                    "The Account");
        } else if (updateable) {
            entity = new AccountWithUpdate(repository, properties, path,
                    "The Account");
        } else {
            entity = new Account(repository, properties, path, "The Account");
        }
        Accounts accounts = resolver.get("/accounts", Accounts.class).get();
        accounts.addEntity(new EntityRelationship(entity, Relationship.ITEM));

        return repository.save(entity);
    }

    public AccountBuilder creationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public AccountBuilder userName(String username) {
        this.username = username;
        return this;
    }

    public void isDeletable(boolean b) {
        this.deletable = true;
    }

    public void isUpdatable(boolean b) {
        this.updateable = true;
    }

}