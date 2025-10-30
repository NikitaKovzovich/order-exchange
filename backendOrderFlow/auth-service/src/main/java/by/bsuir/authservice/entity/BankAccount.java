package by.bsuir.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bank_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "bic", nullable = false, length = 20)
    private String bic;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;
}

