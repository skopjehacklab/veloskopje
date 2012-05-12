function (newDoc, oldDoc, userCtx) {

  function require(field, message) {
    message = message || "Document must have a " + field + " field";
    if (!newDoc[field]) throw({forbidden : message});
  };

  function unchanged(field) {
    if (oldDoc && toJSON(oldDoc[field]) != toJSON(newDoc[field]))
      throw({forbidden : "Field can't be changed: " + field});
  };

  function isAdmin() {
    return userCtx.roles.indexOf('_admin') != -1
  };

  if(!isAdmin()){
    unchanged("image");
    unchanged("comment");
    unchanged("latitude");
    unchanged("longitude");
    unchanged("timestamp");
  }

  if (newDoc) {
    require("image");
    require("comment");
    require("latitude");
    require("longitude");
    require("timestamp");
  }

  if(oldDoc) {
    if(newDoc._deleted && !isAdmin()) {
      throw({forbidden : "You are not allowed to delete this document!"});
    }
  }
}
