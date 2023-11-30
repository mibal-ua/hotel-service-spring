/*
 * Copyright (c) 2023. Mykhailo Balakhon, mailto:9mohapx9@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ua.mibal.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ua.mibal.booking.model.entity.User;
import ua.mibal.booking.model.entity.embeddable.Photo;
import ua.mibal.booking.repository.custom.CustomUserRepository;

import java.util.Optional;

/**
 * @author Mykhailo Balakhon
 * @link <a href="mailto:9mohapx9@gmail.com">email</a>
 */
public interface UserRepository extends JpaRepository<User, Long>, CustomUserRepository {

    Optional<User> findByEmail(String email);

    @Query("""
            select u.password from User u
            where u.email = ?1
            """)
    Optional<String> findPasswordByEmail(String email);

    boolean existsByEmail(String email);

    @Transactional
    void deleteByEmail(String email);

    @Transactional
    @Modifying
    @Query("""
            update User u
                set u.password = ?1
            where u.email = ?2
            """)
    void updateUserPasswordByEmail(String newEncodedPassword, String email);

    @Modifying
    @Query("""
            update User u
                set u.photo = ?1
            where u.email = ?2
            """)
    void updateUserPhotoByEmail(Photo photo, String email);

    @Modifying
    @Query("""
            update User u
                set u.photo = null
            where u.email = ?1
            """)
    void deleteUserPhotoByEmail(String email);

    @Query("""
            select count(a.id) >= 1 from User u
                left join u.reservations r
                left join r.apartment a
                right join a.apartmentType at
            where u.email = ?2 and at.id = ?1
            """)
    boolean userHasReservationWithApartment(String email, Long apartmentId);
}
