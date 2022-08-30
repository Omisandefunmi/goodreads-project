package africa.semicolon.goodreads.models;

import africa.semicolon.goodreads.models.enums.AccountStatus;
import africa.semicolon.goodreads.models.enums.Gender;
import africa.semicolon.goodreads.models.enums.RoleType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Validated
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    public User(String firstName, String lastName, String email, String password){
        this.firstName = firstName;
        this.lastName= lastName;
        this.email = email;
        this.password = password;
        if (roles == null){
            roles = new HashSet<>();
        }
        roles.add(new Role(RoleType.ROLE_USER));
    }
    public User(String firstName, String lastName, String email, String password, RoleType roleType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        if (roles == null){
            roles = new HashSet<>();
        }
        roles.add(new Role(roleType));
    }
    @SequenceGenerator(
            name = "user_id_sequence",
            sequenceName = "user_id_sequence"
    )
    @Id
    @Column(name = "user_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "user_id_sequence")
    private Long id;

    @Size(min = 5, max = 20)
    @NotBlank
    @NotNull
    private String firstName;

    @NotBlank
    @NotNull
    private String lastName;

    @Email
    @Column(unique = true)
    @NotNull
    @NotBlank
    private String email;

    @NotNull
    @NotBlank
    private String password;

    @Enumerated(value = EnumType.STRING)
    private AccountStatus accountStatus;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDateTime dob;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate dateJoined;

    private String location;

    private String isVerified;

    @Enumerated(value = EnumType.STRING)
    private Gender gender;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Role> roles;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

}
