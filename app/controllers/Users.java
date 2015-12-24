package controllers;

import java.util.List;

import annotations.Mobile;

import models.Aco;
import models.Ngo;
import models.Role;
import models.User;
import play.data.validation.Valid;
import play.mvc.With;
import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.ExternalRestrictions;
import controllers.deadbolt.Unrestricted;

@With(Deadbolt.class)
public class Users extends Controller {

	@ExternalRestrictions("View User")
	public static void list() {
		List<User> users = User.find("id <> 1").fetch();
		render(users);
	}

	@ExternalRestrictions("View User")
	public static void details(Long id) {
		User user = User.findById(id);
		notFoundIfNull(user, "user not found");
		
		List<Ngo> ngos;
        if(user.ngos.size() > 0) {
            ngos = Ngo.find("SELECT t FROM Ngo AS t WHERE t NOT IN (:ids) ORDER BY t.name ASC").bind("ids", user.ngos).fetch();
        } else {
            ngos = Ngo.find("ORDER BY name ASC").fetch();
        }
        
		render(user, ngos);
	}
	
	@ExternalRestrictions("Edit User")
    public static void assignNgo(Long userId, Long[] ngos, Boolean remove) {
        notFoundIfNull(userId, "No userId provided");
        if(ngos.length > 0) {
            User user = User.findById(userId);
            notFoundIfNull(user, "user not found");
            for(Long ngoId : ngos) {
                Ngo ngo = Ngo.findById(ngoId);
                if(remove) {
                    user.ngos.remove(ngo);
                } else {
                    user.ngos.add(ngo);
                }
            }
            user.save();
        }
        ok();
    }

	@ExternalRestrictions("Edit User")
	public static void create() {
		List<Role> roles = Role.findAll();
		render("@edit", roles);
	}

	@ExternalRestrictions("Edit User")
	public static void edit(Long id) {
		User user = User.findById(id);
		notFoundIfNull(user, "user not found");
		user.password = null;
		List<Role> roles = Role.findAll();
		render(user, roles);
	}

	@ExternalRestrictions("Edit User")
	public static void submit(@Valid User user) {
		if (validation.hasErrors()) {
			List<Role> roles = Role.findAll();
			render("@edit", user, roles);
		}
		user.save();
		list();
	}

	@ExternalRestrictions("Edit User")
	public static void delete(Long id) {
		if (request.isAjax()) {
			notFoundIfNull(id, "id not provided");
			User user = User.findById(id);
			notFoundIfNull(user, "user not found");
			user.delete();
			ok();
		}
	}

	/* Ngos */
	@ExternalRestrictions("Edit NGO")
	public static void ngoList() {
		List<Ngo> ngos = Ngo.findAll();
		render(ngos);
	}

	@ExternalRestrictions("Edit NGO")
	public static void ngoCreate() {
		render("@ngoEdit");
	}

	@ExternalRestrictions("Edit NGO")
	public static void ngoEdit(Long id) {
		Ngo ngo = Ngo.findById(id);
		notFoundIfNull(ngo, "NGO not found");
		render(ngo);
	}

	@ExternalRestrictions("Edit NGO")
	public static void ngoSubmit(@Valid Ngo ngo) {
		if (validation.hasErrors()) {
			render("@ngoEdit", ngo);
		}
		ngo.save();
		ngoList();
	}

	@ExternalRestrictions("Edit NGO")
	public static void ngoDelete(Long id) {
		if (request.isAjax()) {
			notFoundIfNull(id, "id not provided");
			Ngo ngo = Ngo.findById(id);
			notFoundIfNull(ngo, "ngo not found");
			ngo.delete();
			ok();
		}
	}

	/* Roles */
	@ExternalRestrictions("Edit User")
	public static void roleList() {
		List<Role> roles = Role.find("id <> 1").fetch();
		render(roles);
	}

	@ExternalRestrictions("Edit User")
	public static void roleCreate() {
		render("@roleEdit");
	}

	@ExternalRestrictions("Edit User")
	public static void roleEdit(Long id) {
		Role role = Role.findById(id);
		notFoundIfNull(role, "user not found");
		render(role);
	}

	@ExternalRestrictions("Edit User")
	public static void roleSubmit(@Valid Role role) {
		if (validation.hasErrors()) {
			render("@roleEdit", role);
		}
		role.save();
		roleList();
	}

	@ExternalRestrictions("Edit User")
	public static void roleDelete(Long id) {
		if (request.isAjax()) {
			notFoundIfNull(id, "id not provided");
			Role role = Role.findById(id);
			notFoundIfNull(role, "user not found");
			role.delete();
			ok();
		}
	}

	/* Access Control List */
	@ExternalRestrictions("ACL")
	public static void acl() {
		List<Role> roles = Role.findAll();
		List<Aco> acos = Aco.find("name <> 'ACL'").fetch();
		render(roles, acos);
	}

	@ExternalRestrictions("ACL")
	public static void updatePermission(long acoId, long roleId, boolean state) {
		notFoundIfNull(acoId);
		notFoundIfNull(roleId);
		notFoundIfNull(state);
		Aco aco = Aco.findById(acoId);
		Role role = Role.findById(roleId);
		if (role.id == 1) {
			ok();
		}
		notFoundIfNull(aco);
		notFoundIfNull(role);
		if (state) {
			aco.roles.add(role);
		} else {
			aco.roles.remove(role);
		}
		aco.save();
		ok();
	}

	/*
	 * All mobile API end points are prefixed with the letter 'm'
	 */
	@Unrestricted
	@Mobile
	public static void mLogin() {
		if (!session.contains("apiUser")) {
			error(424, "Session expired");
		}
		User user = User.findByLogin(session.get("apiUser"));
		notFoundIfNull(user);
		renderJSON(new LoginResponse(user));
	}

	public static class LoginResponse {
		public String role;
		public String name;
		public String id;
		public String age;
		public String phone;

		public LoginResponse(User user) {
			this.role = user.role.getRoleName();
			this.name = user.name;
			this.id   = ""+user.id;
			this.age = user.age;
			this.phone = user.phone;
		}
	}
	
}
