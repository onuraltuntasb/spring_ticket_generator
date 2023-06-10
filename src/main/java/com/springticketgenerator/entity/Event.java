package com.springticketgenerator.entity;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
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

    @NotNull
    private int seatCount;

    @NotNull
    private OffsetDateTime startDate;

    @NotNull
    private OffsetDateTime ticketSellingStartDate;

    @NotNull
    private OffsetDateTime endDate;

    @NotNull
    private OffsetDateTime createdAt;

    @NotNull
    private OffsetDateTime updatedAt;

    @NotNull
    private Boolean status;


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

    @Transactional
    public void removeTag(Long tagId) {

        //System.out.println(this.tags);

        Tag tag = this.tags.stream().filter(t -> t.getId() == tagId).findFirst().orElse(null);
        if (tag != null) {

            //System.out.println(tag);

            this.tags.remove(tag);
            tag.getEvents().remove(this);
        }
    }

}
