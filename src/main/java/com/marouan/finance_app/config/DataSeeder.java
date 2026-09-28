package com.marouan.finance_app.config;

import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
@Profile("seed")
public class DataSeeder {

    // just a tiny holder so I can sort events by date before numbering them
    private record FakeEvent(UUID categoryId, BigDecimal amount, String description, OffsetDateTime occurredAt) {}

    @Bean
    CommandLineRunner seedData(JdbcTemplate jdbc) {
        return args -> {
            Faker faker = new Faker();
            var random = ThreadLocalRandom.current();

            // knobs to play with
            int userCount = 100;
            int accountsPerUser = 2;
            int eventsPerAccount = 2_500;   // 200 accounts x 2500 = 500k events

            String[] accountTypes = {"CHECKING", "SAVINGS"};
            String[] expenseCategories = {"Groceries", "Rent", "Transport", "Entertainment", "Utilities"};
            String[] incomeCategories = {"Salary", "Freelance", "Other Income"};

            List<UUID> userIds = new ArrayList<>();
            List<UUID> accountIds = new ArrayList<>();
            // each user gets their own categories, so I keep them grouped per user
            List<List<UUID>> categoriesByUser = new ArrayList<>();

            // users
            List<Object[]> userBatch = new ArrayList<>();
            for (int i = 0; i < userCount; i++) {
                UUID id = UUID.randomUUID();
                userIds.add(id);
                userBatch.add(new Object[]{id, "user" + i, "user" + i + "@example.com", "placeholder_hash"});
            }
            jdbc.batchUpdate(
                    "INSERT INTO users (id, username, email, password_hash) VALUES (?, ?, ?, ?)", userBatch);

            // accounts
            List<Object[]> accountBatch = new ArrayList<>();
            for (UUID userId : userIds) {
                for (int a = 0; a < accountsPerUser; a++) {
                    UUID id = UUID.randomUUID();
                    accountIds.add(id);
                    accountBatch.add(new Object[]{id, userId, faker.finance().iban(),
                            accountTypes[a % accountTypes.length], "EUR"});
                }
            }
            jdbc.batchUpdate(
                    "INSERT INTO accounts (id, user_id, name, type, base_currency) VALUES (?, ?, ?, ?, ?)",
                    accountBatch);

            // categories
            List<Object[]> categoryBatch = new ArrayList<>();
            for (UUID userId : userIds) {
                List<UUID> mine = new ArrayList<>();
                for (String name : expenseCategories) {
                    UUID id = UUID.randomUUID();
                    mine.add(id);
                    categoryBatch.add(new Object[]{id, userId, name, "EXPENSE"});
                }
                for (String name : incomeCategories) {
                    UUID id = UUID.randomUUID();
                    mine.add(id);
                    categoryBatch.add(new Object[]{id, userId, name, "INCOME"});
                }
                categoriesByUser.add(mine);
            }
            jdbc.batchUpdate(
                    "INSERT INTO categories (id, user_id, name, type) VALUES (?, ?, ?, ?)", categoryBatch);

            System.out.println("Users, accounts and categories done. Now the events...");

            // events, one account at a time
            for (int acc = 0; acc < accountIds.size(); acc++) {
                UUID accountId = accountIds.get(acc);
                // accounts were created 2 per user, so this finds the owner's categories
                List<UUID> cats = categoriesByUser.get(acc / accountsPerUser);

                List<FakeEvent> events = new ArrayList<>(eventsPerAccount);
                for (int i = 0; i < eventsPerAccount; i++) {
                    BigDecimal amount = BigDecimal.valueOf(random.nextDouble(-500, 3000))
                            .setScale(2, RoundingMode.HALF_UP);
                    events.add(new FakeEvent(
                            cats.get(random.nextInt(cats.size())),
                            amount,
                            faker.commerce().productName(),
                            OffsetDateTime.now().minusDays(random.nextInt(730))));
                }

                // sequence_no has to follow time order, so sort first, number after
                events.sort(Comparator.comparing(FakeEvent::occurredAt));

                List<Object[]> batch = new ArrayList<>(eventsPerAccount);
                long seq = 1;
                for (FakeEvent e : events) {
                    String type = e.amount().signum() >= 0 ? "CREDIT" : "DEBIT";
                    batch.add(new Object[]{UUID.randomUUID(), accountId, seq++, type,
                            e.amount(), "EUR", e.categoryId(), e.description(), e.occurredAt()});
                }
                jdbc.batchUpdate("""
                        INSERT INTO account_events
                            (id, account_id, sequence_no, event_type, amount, currency,
                             category_id, description, occurred_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """, batch);

                if ((acc + 1) % 20 == 0) {
                    System.out.println("Done " + (acc + 1) + " / " + accountIds.size() + " accounts");
                }
            }

            // the seeder bypasses the service, so I have to fix the derived stuff by hand
            // otherwise the cached balance stays at 0 and the view stays empty
            jdbc.execute("""
                    UPDATE accounts a
                    SET balance = COALESCE((SELECT SUM(amount) FROM account_events e WHERE e.account_id = a.id), 0)
                    """);
            jdbc.execute("REFRESH MATERIALIZED VIEW monthly_category_spend");
            jdbc.execute("ANALYZE account_events");

            System.out.println("Seeding complete: " + (accountIds.size() * eventsPerAccount)
                    + " events across " + accountIds.size() + " accounts.");
        };
    }
}