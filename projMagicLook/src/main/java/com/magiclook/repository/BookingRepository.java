package com.magiclook.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.Date;
import java.util.List;
import com.magiclook.data.*;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

       List<Booking> findByUser(User user);

       List<Booking> findByUserOrderByCreatedAtDesc(User user);

       @Query("SELECT COUNT(b) FROM Booking b WHERE b.item.itemId = :itemId " +
                     "AND b.state != 'CANCELLED' " +
                     "AND ((b.pickupDate <= :returnDate AND b.returnDate >= :pickupDate) OR " +
                     "(b.startUseDate <= :endUseDate AND b.endUseDate >= :startUseDate))")
       Long countOverlappingBookings(@Param("itemId") Integer itemId,
                     @Param("pickupDate") Date pickupDate,
                     @Param("startUseDate") Date startUseDate,
                     @Param("endUseDate") Date endUseDate,
                     @Param("returnDate") Date returnDate);

       @Query("SELECT b FROM Booking b WHERE b.item.itemId = :itemId " +
                     "AND b.state != 'CANCELLED' " +
                     "AND ((b.pickupDate <= :laundryDate AND b.returnDate >= :pickupDate) " +
                     "OR (b.startUseDate <= :endUseDate AND b.endUseDate >= :startUseDate))")
       List<Booking> findOverlappingBookings(
                     @Param("itemId") Integer itemId,
                     @Param("pickupDate") Date pickupDate,
                     @Param("startUseDate") Date startUseDate,
                     @Param("endUseDate") Date endUseDate,
                     @Param("laundryDate") Date laundryDate);

       @Query("SELECT COUNT(b) FROM Booking b WHERE b.item.itemId = :itemId " +
                     "AND b.state != 'CANCELLED' " +
                     "AND NOT (b.endUseDate < :startUseDate OR b.startUseDate > :endUseDate)")
       Long countOverlappingSimple(
                     @Param("itemId") Integer itemId,
                     @Param("startUseDate") Date startUseDate,
                     @Param("endUseDate") Date endUseDate);

       @Query("SELECT COUNT(b) FROM Booking b WHERE b.itemSingle.id = :itemSingleId " +
                     "AND b.state != 'CANCELLED' " +
                     "AND ((b.pickupDate <= :returnDate AND b.returnDate >= :pickupDate) OR " +
                     "(b.startUseDate <= :endUseDate AND b.endUseDate >= :startUseDate))")
       Long countOverlappingBookingsForItemSingle(
                     @Param("itemSingleId") UUID itemSingleId,
                     @Param("pickupDate") Date pickupDate,
                     @Param("startUseDate") Date startUseDate,
                     @Param("endUseDate") Date endUseDate,
                     @Param("returnDate") Date returnDate);

       @Query("SELECT b FROM Booking b WHERE b.itemSingle = :itemSingle " +
                     "AND b.state != 'CANCELLED' " +
                     "AND ((b.pickupDate <= :laundryDate AND b.returnDate >= :pickupDate) " +
                     "OR (b.startUseDate <= :endUseDate AND b.endUseDate >= :startUseDate))")
       List<Booking> findOverlappingBookingsForItemSingle(
                     @Param("itemSingle") ItemSingle itemSingle,
                     @Param("pickupDate") Date pickupDate,
                     @Param("startUseDate") Date startUseDate,
                     @Param("endUseDate") Date endUseDate,
                     @Param("laundryDate") Date laundryDate);
}