package com.example.Account2.domain;

import com.example.Account2.exception.AccountException;
import com.example.Account2.type.AccountStatus;
import com.example.Account2.type.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Account{
    @Id
    @GeneratedValue
    private Long id;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne
    private AccountUser accountUser;
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;
    private long balance;

    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;
    public void useBalance(Long amount){
        if (amount > balance){
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }
        balance -= amount;
    }


    public void cancelBalance(Long amount) {
        if(amount < 0) throw new AccountException(ErrorCode.INVALID_REQUEST);

        balance += amount;
    }
}
