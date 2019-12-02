CREATE TABLE Member
(
 firstName VARCHAR(255) NOT NULL,
 lastName VARCHAR(255) NOT NULL,
 emailAddress VARCHAR(255) NOT NULL,
 isPayingMember CHAR(1) CHECK(isPayingMember IN ('y','n')) NOT NULL,
 isStaffMember CHAR(1) CHECK(isStaffMember IN ('y','n')) NOT NULL,
 isProblemContributor CHAR(1) CHECK(isProblemContributor IN ('y','n')) NOT NULL,
 subscriptionStartDate DATE,
 PRIMARY KEY (emailAddress)
);

CREATE TABLE Problem
(
 title VARCHAR(255) NOT NULL,
 difficulty VARCHAR(10) NOT NULL,
 type VARCHAR(20) CHECK(type IN ('algorithm','database','dataStructure')) NOT NULL,
 ContributorEmail VARCHAR(255) NOT NULL,
 StaffEmail VARCHAR(255),
 numberOfRevisions INTEGER NOT NULL,
 PRIMARY KEY (title),
 FOREIGN KEY (ContributorEmail ) REFERENCES Member (emailAddress),
 FOREIGN KEY (StaffEmail ) REFERENCES Member (emailAddress)
);

CREATE TABLE BuildUpon
(
 thisProblem VARCHAR(255) NOT NULL,
 otherProblem VARCHAR(255) NOT NULL,
 PRIMARY KEY (thisProblem, otherProblem),
 FOREIGN KEY (thisProblem ) REFERENCES Problem (title),
 FOREIGN KEY (otherProblem ) REFERENCES Problem (title)
);

CREATE TABLE RelatedTopic
(
 title VARCHAR(255) NOT NULL,
 topicName VARCHAR(255) NOT NULL,
 PRIMARY KEY (title, topicName),
 FOREIGN KEY (title) REFERENCES Problem (title)
);

CREATE TABLE ProblemsPool
(
 emailAddress VARCHAR(255) NOT NULL,
 poolName VARCHAR(255) NOT NULL,
 PRIMARY KEY (emailAddress, poolName),
 FOREIGN KEY (emailAddress) REFERENCES Member (emailAddress)
);

CREATE TABLE ComposedOf
(
 title VARCHAR(255) NOT NULL,
 emailAddress VARCHAR(255) NOT NULL,
 poolName VARCHAR(255) NOT NULL,
 PRIMARY KEY (title, emailAddress, poolName),
 FOREIGN KEY (title) REFERENCES Problem (title),
 FOREIGN KEY (emailAddress, poolName) REFERENCES ProblemsPool (emailAddress, poolName)
);

INSERT INTO Member VALUES('Forest','Gump','forest@bgshrimp.com','y','n','y',TO_DATE('042001','MMDDYY'));
INSERT INTO Member VALUES('Fred','Rodgers','frodgers@wqed.org','y','n','y',TO_DATE('082918','MMDDYY'));

INSERT INTO Problem VALUES('string declerations in multiple languages','medium','algorithm','forest@bgshrimp.com',NULL,7);
INSERT INTO Problem VALUES('intro to select queries','easy','database','frodgers@wqed.org',NULL,3);
INSERT INTO Problem VALUES('searching trees with varible length leaves','hard','algorithm','forest@bgshrimp.com',NULL,2);
INSERT INTO Problem VALUES('joining tables','easy','database','frodgers@wqed.org',NULL,1);
INSERT INTO Problem VALUES('Java Array Lists','medium','dataStructure','forest@bgshrimp.com',NULL,4);
INSERT INTO Problem VALUES('ordering results','easy','database','frodgers@wqed.org',NULL,-1);

INSERT INTO ProblemsPool VALUES('forest@bgshrimp.com','compare to Java');
INSERT INTO ProblemsPool VALUES('forest@bgshrimp.com','Java data objects');

INSERT INTO ComposedOf VALUES('Java Array Lists','forest@bgshrimp.com','compare to Java');
INSERT INTO ComposedOf VALUES('searching trees with varible length leaves','forest@bgshrimp.com','compare to Java');
INSERT INTO ComposedOf VALUES('string declerations in multiple languages','forest@bgshrimp.com','compare to Java');
INSERT INTO ComposedOf VALUES('Java Array Lists','forest@bgshrimp.com','Java data objects');
