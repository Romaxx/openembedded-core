SUMMARY = "PCI utilities"
DESCRIPTION = 'The PCI Utilities package contains a library for portable access \
to PCI bus configuration space and several utilities based on this library.'
HOMEPAGE = "http://atrey.karlin.mff.cuni.cz/~mj/pciutils.shtml"
SECTION = "console/utils"

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=751419260aa954499f7abaabaa882bbe"
DEPENDS = "zlib kmod"

SRC_URI = "${KERNELORG_MIRROR}/software/utils/pciutils/pciutils-${PV}.tar.xz \
           file://configure.patch"

SRC_URI[md5sum] = "8e52278399b7b35fcfc031bc05da80a8"
SRC_URI[sha256sum] = "f346eeb90cce0910c05b877fe49eadc760fa084c0455fd313e39d4b2c2d4bb21"

inherit multilib_header pkgconfig

PACKAGECONFIG ??= "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'hwdb', '', d)}"
PACKAGECONFIG[hwdb] = "HWDB=yes,HWDB=no,udev"

PCI_CONF_FLAG = "ZLIB=yes DNS=yes SHARED=yes STRIP= LIBDIR=${libdir}"

# see configure.patch
do_configure () {
	(
	  cd lib && \
	  # PACKAGECONFIG_CONFARGS for this recipe could only possibly contain 'HWDB=yes/no',
	  # so we put it before ./configure
	  ${PCI_CONF_FLAG} ${PACKAGECONFIG_CONFARGS} ./configure ${PV} ${datadir} ${TARGET_OS} ${TARGET_ARCH}
	)
}

export PREFIX = "${prefix}"
export SBINDIR = "${sbindir}"
export SHAREDIR = "${datadir}"
export MANDIR = "${mandir}"

EXTRA_OEMAKE = "-e MAKEFLAGS= ${PCI_CONF_FLAG}"

ASNEEDED = ""

# The configure script breaks if the HOST variable is set
HOST[unexport] = "1"

do_install () {
	oe_runmake DESTDIR=${D} install install-lib

	install -d ${D}${bindir}
	ln -s ../sbin/lspci ${D}${bindir}/lspci

	oe_multilib_header pci/config.h
}

PACKAGES =+ "${PN}-ids libpci"
FILES_${PN}-ids = "${datadir}/pci.ids*"
FILES_libpci = "${libdir}/libpci.so.*"
SUMMARY_${PN}-ids = "PCI utilities - device ID database"
DESCRIPTION_${PN}-ids = "Package providing the PCI device ID database for pciutils."
RDEPENDS_${PN} += "${PN}-ids"
