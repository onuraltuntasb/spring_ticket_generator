package com.springtickergenerator.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String name;

    @NotBlank
    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private Date createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private Date updatedAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private Date endDate;

    @NotNull
    private Boolean status;

    private int seatCount;

    //comma seperated
    @NotBlank
    private String availableSeats;


    @ManyToMany(
            fetch = FetchType.LAZY,
            cascade ={CascadeType.PERSIST,CascadeType.MERGE})
    @JoinTable(name="event_tag",
            joinColumns = {@JoinColumn(name="event_id")},
            inverseJoinColumns = {@JoinColumn(name="tag_id")})
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JoinColumn(name="event_id",referencedColumnName = "id")
    private Set<Ticket> tickets = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

}
