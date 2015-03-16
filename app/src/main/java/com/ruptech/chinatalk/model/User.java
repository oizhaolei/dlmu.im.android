package com.ruptech.chinatalk.model;

import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A data class representing Basic user information element
 */
public class User extends Item implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1311784597606188334L;

	public String password;
	public int wallet_close;
	public String tel;
	public int is_public;
	public int is_service;
	public int gender;
	public String fullname;
	public String lang;
	public int active;
	public String user_memo;
	public double balance;
	public int point;
	public String pic_url;
	private int delete_flag;
	public String create_id;
	public String update_id;
	public long like_id;
	public String like_date;
	public Date update_date;

	public String lang1;

	public String lang2;
	private String[] additionalLangs;

	public int is_volunteer;

	private int follow_count;

	private int follower_count;

	private int album_count;

	private String background_url;

	public int late6;

	public int lnge6;

	private int present_count;

	private int level;

	private int charm_point;

	private int exp_point;

    public String terminal_type;

    public int account_valid;

	public User() {
	}

	public User(JSONObject json) throws JSONException {
		id = json.getLong("id");
		password = json.optString("password");
		tel = json.optString("tel");
		like_id = json.optLong("like_id");
		like_date = json.optString("like_date", "");
		fullname = json.optString("fullname");
		lang = json.optString("lang");
		lang1 = json.optString("lang1", "");
		lang2 = json.optString("lang2", "");
		active = json.optInt("active");
		gender = json.optInt("gender");
		user_memo = json.optString("user_memo");
		balance = json.optDouble("balance");
		point = json.optInt("point");
		pic_url = json.optString("pic_url");
		delete_flag = json.optInt("delete_flag");
		level = json.optInt("level");

		try {
			if (json.has("prop")) {

				JSONObject item = json.getJSONObject("prop");
				if (item.has("additional_languages")) {
					String additionalLangStr = item
							.getString("additional_languages");
					additionalLangs = additionalLangStr.split(",");
				}
				if (item.has("follow_count")) {
					follow_count = item.optInt("follow_count");
				}
				if (item.has("follower_count")) {
					follower_count = item.optInt("follower_count");
				}
				if (item.has("album_count")) {
					album_count = item.optInt("album_count");
				}
				if (item.has("background_url")) {
					background_url = item.optString("background_url");
				}
				if (item.has("present_count")) {
					present_count = item.optInt("present_count");
				}
				if (item.has("level")) {
					level = item.optInt("level");
				}
				if (item.has("charm_point")) {
					charm_point = item.optInt("charm_point");
				}
				if (item.has("exp_point")) {
					exp_point = item.optInt("exp_point");
				}
			}
		} catch (Exception e) {
		}
        terminal_type = json.optString("terminal_type");
		create_id = json.optString("create_id");
		create_date = DateCommonUtils.parseToDateFromString(json
				.optString("create_date"));
		update_id = json.optString("update_id");
		update_date = DateCommonUtils.parseToDateFromString(json
				.optString("update_date"));
		if (json.has("is_volunteer")) {
			is_volunteer = json.optInt("is_volunteer");
		}
		if (json.has("late6")) {
			late6 = json.optInt("late6");
		}
		if (json.has("lnge6")) {
			lnge6 = json.optInt("lnge6");
		}

        account_valid = json.optInt("account_valid", 0);
	}

	public long getLike_id() {
		return like_id;
	}

	public void setLike_id(long like_id) {
		this.like_id = like_id;
	}

	public int getPresent_count() {
		return present_count;
	}

	public void setPresent_count(int present_count) {
		this.present_count = present_count;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getCharm_point() {
		return charm_point;
	}

	public void setCharm_point(int charm_point) {
		this.charm_point = charm_point;
	}

	public int getExp_point() {
		return exp_point;
	}

	public void setExp_point(int exp_point) {
		this.exp_point = exp_point;
	}

	public int getActive() {
		return active;
	}

	public String[] getAdditionalLangs() {
		return additionalLangs;
	}

	public int getAlbum_count() {
		return album_count;
	}

	public List<String> getAllLangs() {
		List<String> allLangList = new ArrayList<>();
		allLangList.add(lang);
		if (additionalLangs != null) {
			for (String l : additionalLangs) {
				allLangList.add(l);
			}
		}
		return allLangList;
	}

	public String getBackground_url() {
		return background_url;
	}

	public double getBalance() {
		return balance;
	}

    public String getTerminal_type() {
        return terminal_type;
    }

	public String getCreate_id() {
		return create_id;
	}

	public int getDelete_flag() {
		return delete_flag;
	}

	public int getFollow_count() {
		return follow_count;
	}

	public int getFollower_count() {
		return follower_count;
	}

	public String getFullname() {
		if (Utils.isEmpty(fullname)) {
			return tel;
		}
		return fullname;
	}

	public int getGender() {
		return gender;
	}

	public int getIs_public() {
		return is_public;
	}

	public int getIs_service() {
		return is_service;
	}

	public int getIs_volunteer() {
		return is_volunteer;
	}

	public String getLang() {
		return lang;
	}

	public String getLang1() {
		return lang1;
	}

	public String getLang2() {
		return lang2;
	}

	public int getLate6() {
		return late6;
	}

	public String getLike_date() {
		return like_date;
	}

	public long getLikeId() {
		return this.like_id;
	}

	public int getLnge6() {
		return lnge6;
	}

	public String getPassword() {
		return password;
	}

	public String getPic_url() {
		return pic_url;
	}

	public int getPoint() {
		return point;
	}

	public String getTel() {
		return tel;
	}

	public Date getUpdate_date() {
		return update_date;
	}

	public String getUpdate_id() {
		return update_id;
	}

	public String getUser_memo() {
		return user_memo;
	}

	public int getWallet_close() {
		return wallet_close;
	}

    public int getAccount_valid() {
        return account_valid;
    }

	public boolean isCanTranslate(String langStr) {

		if (!Utils.isEmpty(langStr)) {
			List<String> allLangs = getAllLangs();
			return allLangs.size() > 1 && allLangs.contains(langStr);
		}
		return false;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public void setAdditionalLangs(String[] additionalLangList) {
		this.additionalLangs = additionalLangList;
	}

	public void setAlbum_count(int album_count) {
		this.album_count = album_count;
	}

	public void setBackground_url(String background_url) {
		this.background_url = background_url;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public void setCreate_id(String create_id) {
		this.create_id = create_id;
	}

	public void setDelete_flag(int delete_flag) {
		this.delete_flag = delete_flag;
	}

	public void setFollow_count(int follow_count) {
		this.follow_count = follow_count;
	}

	public void setFollower_count(int follower_count) {
		this.follower_count = follower_count;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public void setIs_public(int is_public) {
		this.is_public = is_public;
	}

	public void setIs_service(int is_service) {
		this.is_service = is_service;
	}

	public void setIs_volunteer(int is_volunteer) {
		this.is_volunteer = is_volunteer;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public void setLang1(String lang1) {
		this.lang1 = lang1;
	}

	public void setLang2(String lang2) {
		this.lang2 = lang2;
	}

	public void setLate6(int late6) {
		this.late6 = late6;
	}

	public void setLike_date(String like_date) {
		this.like_date = like_date;
	}

	public void setLikeId(long like_id) {
		this.like_id = like_id;
	}

	public void setLnge6(int lnge6) {
		this.lnge6 = lnge6;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPic_url(String pic_url) {
		this.pic_url = pic_url;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

    public void setTerminal_type(String terminal_type) {
        this.terminal_type = terminal_type;
    }

	public void setUpdate_date(Date update_date) {
		this.update_date = update_date;
	}

	public void setUpdate_id(String update_id) {
		this.update_id = update_id;
	}

	public void setUser_memo(String user_memo) {
		this.user_memo = user_memo;
	}

	public void setWallet_close(int wallet_close) {
		this.wallet_close = wallet_close;
	}

    public void setAccount_valid(int account_valid) {
        this.account_valid = account_valid;
    }

    @Override
	public String toString() {
		return "User [id=" + id + ", tel=" + tel + "]";
	}

    public String getOF_JabberID(){
        return String.format("chinatalk_%d@tttalk.org", id);
    }
}
