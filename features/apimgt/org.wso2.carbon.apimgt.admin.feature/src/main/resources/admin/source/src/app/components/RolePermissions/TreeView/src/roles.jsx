import React, { useState } from "react";
import { makeStyles } from "@material-ui/core/styles";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import Paper from "@material-ui/core/Paper";
import TextField from "@material-ui/core/TextField";
import IconButton from "@material-ui/core/IconButton";
import AddIcon from "@material-ui/icons/Add";
import clsx from "clsx";

import tenantConfig from "./tenant-config.json";
import Permissions from "./permisions";

const useStyles = makeStyles({
  table: {
    minWidth: 650
  },
  c3: {
    backgroundColor: "yellow"
  },
  c1: {
    backgroundColor: "red"
  },
  c2: {
    backgroundColor: "green"
  }
});

function rolesSet(config) {
  const roles = new Set();
  for (const mapping of config) {
    mapping.Roles.split(",").forEach(scope => roles.add(scope.trim()));
  }
  return [...roles];
}

export default function SimpleTable() {
  const classes = useStyles();
  const [newRole, setNewRole] = useState("");
  const [roles, setRoles] = useState(rolesSet(tenantConfig.Scope));

  const onAddEntry = () => {
    if (roles.find(role => role === newRole) || !newRole) {
      alert("Role already exsists or role empty !!");
      return;
    }
    setRoles([...roles, newRole]);
    setNewRole("");
  };
  return (
    <>
      <TextField
        value={newRole}
        label="New Value"
        variant="outlined"
        onChange={({ target: { value } }) => {
          setNewRole(value);
        }}
        onKeyDown={event =>
          (event.which === 13 ||
            event.keyCode === 13 ||
            event.key === "Enter") &&
          onAddEntry()
        }
      />
      <IconButton
        onClick={onAddEntry}
        aria-label="delete"
        className={classes.margin}
      >
        <AddIcon fontSize="small" />
      </IconButton>

      <TableContainer component={Paper}>
        <Table
          className={clsx(classes.table, classes.c3)}
          aria-label="simple table"
        >
          <TableHead>
            <TableRow>
              <TableCell>Role</TableCell>
              <TableCell align="right">Permissions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {roles.map(role => (
              <TableRow key={role}>
                <TableCell component="th" scope="row">
                  {role}
                </TableCell>
                <TableCell align="right">
                  <Permissions />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </>
  );
}
