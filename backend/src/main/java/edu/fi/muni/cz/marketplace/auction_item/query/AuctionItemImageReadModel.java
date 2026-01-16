package edu.fi.muni.cz.marketplace.auction_item.query;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auction_item_images")
@Data
@NoArgsConstructor
public class AuctionItemImageReadModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "auction_item_id")
    private UUID auctionItemId;

    @Column(nullable = false, name = "image_url")
    private String imageUrl;

    public AuctionItemImageReadModel(UUID auctionItemId, String imageUrl) {
        this.auctionItemId = auctionItemId;
        this.imageUrl = imageUrl;
    }
}
