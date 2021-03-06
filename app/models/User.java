/*
 * Copyright (C) 2011 mPower Health
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package models;

import play.*;
import play.data.validation.Email;
import play.data.validation.Equals;
import play.data.validation.Match;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Password;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.*;
import play.libs.Crypto;
import play.libs.Crypto.HashType;

import javax.persistence.*;

import models.deadbolt.RoleHolder;

import java.util.*;

/**
 * User Model - Contains user data.
 */

@Entity
public class User extends Model implements RoleHolder {

    @Required
    @Unique
    @MaxSize(15)
    @MinSize(4)
    @Match(value="^\\w*$", message="Not a valid username")
    public String login;

    @MaxSize(100)
    public String name;
    
    public String phone;
    public String age;
    public String location;

    @Required
    @MaxSize(15)
    @MinSize(5)
    @Password
    public String password;

    @Transient
    @Equals(value="password", message="Password doesn't match")
    @Password
    public String confirmPassword;

    @Required
    @ManyToOne
    public Role role;
    
    @ManyToMany
    @OrderBy("name ASC")
    public Set<Ngo> ngos;

    /**
     * @param email
     * @param password
     */
    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    /**
     * @param email
     * @param password
     * @param name
     */
    public User(String email, String password, String name) {
    	this(email, password);
        this.login = name;
    }

    public User(String email, String password, String name, Role role) {
    	this(email, password, name);
        this.role = role;
    }

	// From RoleHolder Interface
	@Override
	public List<? extends Role> getRoles() {
		List<Role> list = new ArrayList<Role>();
		list.add(this.role);
		return list;
	}

	/**
	 * Authenticate.
	 *
	 * @param username the username
	 * @param password the password
	 * @return the user
	 */
	public static User authenticate(String username, String password) {
		return User.find("byLoginAndPassword", username, Crypto.passwordHash(password, HashType.SHA512)).first();
    }

	public static User findByLogin(String username) {
		return User.find("byLogin", username).first();
	}

	@PrePersist
	@PreUpdate
	public void updatePassword() {
	    this.password = Crypto.passwordHash(this.password, HashType.SHA512);
	}

	public String toString() {
		return this.id + " " + this.login + " " + this.name + " ";
	}
}