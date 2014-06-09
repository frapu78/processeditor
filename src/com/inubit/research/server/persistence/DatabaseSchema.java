/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.persistence;

/**
 * Collection of constants related to the DatabaseSchema
 * @author fel
 */
public class DatabaseSchema {

    public enum Table {
        USER("users"),
        GROUPS("groups"),
        SUBGROUPS("subgroups"),
        USER_IMAGE("userimage"),
        MODEL("models"),
        VERSIONS("versions"),
        EVOLUTION("evolution"),
        COMMENTS("comments"),
        ACCESS("access"),
        PATHACCESS("pathaccess"),
        IS("isids"),
        CONNECTIONS("connections");

        private String tableName;

        Table(String tableName) {
            this.tableName = tableName;
        }

        public String getName() {
            return this.tableName;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    public enum Attribute {
        //Schema:<Table>_<attribute>
        USER_NAME("name", Type.STRING),
        USER_PWD("pwd", Type.STRING),
        USER_ADMIN("admin", Type.BOOL),
        USER_IMAGE("imageid", Type.OTHER),
        USER_REALNAME("realname", Type.STRING),
        USER_MAIL("mail", Type.STRING),

        USER_IMAGE_ID("id", Type.OTHER),
        USER_IMAGE_IMAGE("image", Type.OTHER),
        USER_IMAGE_TYPE("imagetype", Type.ENUM),

        VERSION_ID("id", Type.STRING),
        VERSION_VERSION("version", Type.OTHER),
        VERSION_MODEL("model", Type.OTHER),
        VERSION_COMMENT("comment", Type.STRING),
        VERSION_CREATED("created", Type.DATE),
        VERSION_USER("username", Type.STRING),

        MODEL_ID("id", Type.STRING),
        MODEL_OWNER("owner", Type.STRING),
        MODEL_PATH("path", Type.STRING),
        MODEL_AUTHOR("author", Type.STRING),
        MODEL_NAME("name", Type.STRING),
        MODEL_COMMENT("comment", Type.STRING),
        MODEL_CREATION_DATE("created", Type.DATE),

        ACCESS_ID("id", Type.STRING),
        ACCESS_NAME("name", Type.STRING),
        ACCESS_TYPE("nametype", Type.ENUM),
        ACCESS_ACCESS("accesstype", Type.ENUM),

        EVOLUTION_ID("id", Type.STRING),
        EVOLUTION_VERSION("version", Type.OTHER),
        EVOLUTION_SUCCESSOR("successor", Type.OTHER),

        COMMENT_ID("id", Type.OTHER),
        COMMENT_MODEL("modelid", Type.STRING),
        COMMENT_ELEMENT("elementid", Type.STRING),
        COMMENT_VALIDFROM("validfrom", Type.OTHER),
        COMMENT_VALIDUNTIL("validuntil", Type.OTHER),
        COMMENT_USER("username", Type.STRING),
        COMMENT_TEXT("text", Type.STRING),
        COMMENT_CREATED("created", Type.DATE),

        CONNECTIONS_USER("username", Type.STRING),
        CONNECTIONS_URL("url", Type.STRING),
        CONNECTIONS_ISUSER("isusername", Type.STRING),
        CONNECTIONS_PWD("pwd", Type.STRING),

        IS_HOST("host", Type.STRING),
        IS_URI("uri", Type.STRING),
        IS_ID("id", Type.STRING),

        GROUP_NAME("name", Type.STRING),
        GROUP_USER("username", Type.STRING),

        SUBGROUP_NAME("name", Type.STRING),
        SUBGROUP_SUBGROUP("sub", Type.STRING);

        private enum Type {
            STRING,
            ENUM,
            DATE,
            BOOL,
            OTHER
        }

        private String attributeName;
        private Type attributeType;

        Attribute( String name, Type type ) {
            this.attributeName = name;
            this.attributeType = type;
        }

        public String getName() {
            return this.attributeName;
        }

        public Object encodeValue( Object v ) {
            if (this.attributeType.equals(Type.OTHER))
                return v;
            else
                return "'" + v + "'";
        }

        public boolean isEnum() {
            return this.attributeType.equals(Type.ENUM);
        }

        public boolean isBoolean() {
            return this.attributeType.equals(Type.BOOL);
        }

        @Override
        public String toString() {
            return getName();
        }

    }


    /*
     * Attribute names
     * Schema: ATTR_<Table>_<attribute>
     */
//    public static final String ATTR_USER_NAME = "name";
//    public static final String ATTR_USER_PWD = "pwd";
//    public static final String ATTR_USER_ADMIN = "admin";
//    public static final String ATTR_USER_IMAGE = "imageid";
//    public static final String ATTR_USER_REALNAME = "realname";
//    public static final String ATTR_USER_MAIL = "mail";
//
//    public static final String ATTR_USER_IMAGE_ID = "id";
//    public static final String ATTR_USER_IMAGE_IMAGE = "image";
//    public static final String ATTR_USER_IMAGE_TYPE = "imagetype";
//
//    public static final String ATTR_VERSION_ID = "id";
//    public static final String ATTR_VERSION_VERSION = "version";
//    public static final String ATTR_VERSION_MODEL = "model";
//    public static final String ATTR_VERSION_COMMENT = "comment";
//    public static final String ATTR_VERSION_CREATED = "created";
//    public static final String ATTR_VERSION_USER = "username";
//
//    public static final String ATTR_MODEL_ID = "id";
//    public static final String ATTR_MODEL_OWNER = "owner";
//    public static final String ATTR_MODEL_PATH = "path";
//    public static final String ATTR_MODEL_AUTHOR = "author";
//    public static final String ATTR_MODEL_NAME = "name";
//    public static final String ATTR_MODEL_COMMENT = "comment";
//    public static final String ATTR_MODEL_CREATION_DATE = "created";
//
//    public static final String ATTR_ACCESS_ID = "id";
//    public static final String ATTR_ACCESS_NAME = "name";
//    public static final String ATTR_ACCESS_TYPE = "nametype";
//    public static final String ATTR_ACCESS_ACCESS = "accesstype";
//
//    public static final String ATTR_EVOLUTION_ID = "id";
//    public static final String ATTR_EVOLUTION_VERSION = "version";
//    public static final String ATTR_EVOLUTION_SUCCESSOR = "successor";
//
//    public static final String ATTR_COMMENT_ID = "id";
//    public static final String ATTR_COMMENT_MODEL = "modelid";
//    public static final String ATTR_COMMENT_ELEMENT = "elementid";
//    public static final String ATTR_COMMENT_VALIDFROM = "validfrom";
//    public static final String ATTR_COMMENT_VALIDUNTIL = "validuntil";
//    public static final String ATTR_COMMENT_USER = "username";
//    public static final String ATTR_COMMENT_TEXT = "text";
//    public static final String ATTR_COMMENT_CREATED = "created";
}
